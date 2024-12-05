package ch.ifocusit.order.domain.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ch.ifocusit.order.domain.model.Order;
import ch.ifocusit.order.domain.model.OrderStatus;
import ch.ifocusit.order.domain.model.events.NewOrderEvent;
import ch.ifocusit.order.domain.model.exception.NotCancellableException;
import ch.ifocusit.order.domain.model.exception.QuantityException;
import ch.ifocusit.order.domain.port.OrderProcessEventPublisher;
import ch.ifocusit.order.domain.port.OrderRepository;
import ch.ifocusit.order.domain.port.ProductStore;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    OrderRepository repository;

    @Mock
    ProductStore productStore;

    @Mock
    OrderProcessEventPublisher eventPublisher;

    @InjectMocks
    OrderService service;

    @Test
    @DisplayName("should list all orders in database")
    void orders() {
        // given
        Order order1 = mock(Order.class);
        Order order2 = mock(Order.class);
        when(repository.all()).thenReturn(Multi.createFrom().items(order1, order2));

        // when
        var orders = service.orders()
                .subscribe().withSubscriber(AssertSubscriber.create(2))
                // then
                .assertCompleted()
                .getItems();

        assertThat(orders).containsOnly(order1, order2);
    }

    @Test
    @DisplayName("create a new order in database")
    void executeNewOrder() {
        // given
        when(productStore.ifAvailable(anyString(), anyInt()))
                .thenReturn(Uni.createFrom().voidItem());

        when(repository.persist(any())).thenAnswer((invocation) -> {
            Order order = invocation.getArgument(0);
            return Uni.createFrom().item(order.toBuilder().id("123").build());
        });

        var event = NewOrderEvent.builder()
                .productId("chaussettes")
                .quantity(100)
                .build();

        // when
        var order = service.execute(event)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                // then
                .assertCompleted()
                .getItem();

        assertThat(order).isNotNull();
        assertThat(order.getId()).isEqualTo("123");
        assertThat(order.getProductId()).isEqualTo("chaussettes");
        assertThat(order.getQuantity()).isEqualTo(100);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.EXECUTED);

        verify(productStore).ifAvailable("chaussettes", 100);
        verify(eventPublisher).publishExecutedEvent(order);
    }

    @Test
    @DisplayName("create a new order should validate the quantity")
    void createShouldThrowException() {
        // given
        when(productStore.ifAvailable(anyString(), anyInt()))
                .thenReturn(Uni.createFrom().voidItem());

        var event = NewOrderEvent.builder()
                .productId("chaussettes")
                .quantity(-10)
                .build();

        // when
        var exception = service.execute(event)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                // then
                .assertFailed()
                .getFailure();

        // then
        assertThat(exception)
                .isInstanceOf(QuantityException.class)
                .asInstanceOf(InstanceOfAssertFactories.type(QuantityException.class))
                .satisfies(e -> {
                    assertThat(e.getQuantity()).isEqualTo(-10);
                    assertThat(e.getReferenceQuantity()).isEqualTo(0);
                });

        verify(repository, never()).persist(any());
    }

    @Test
    @DisplayName("cancel a new order should update his status")
    void cancelShouldChangeStatus() {
        // given
        Order existing = Order.builder()
                .id("123")
                .productId("chaussettes")
                .quantity(10)
                .status(OrderStatus.NEW)
                .build();

        when(repository.findById("123")).thenReturn(Uni.createFrom().item(existing));

        when(repository.persist(any())).thenAnswer((invocation) -> {
            Order order = invocation.getArgument(0);
            return Uni.createFrom().item(order.toBuilder().id("456").build());
        });

        // when
        var order = service.cancel("123")
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                // then
                .assertCompleted()
                .getItem();

        assertThat(order).isNotNull();
        assertThat(order.getId()).isEqualTo("456");
        assertThat(order.getProductId()).isEqualTo("chaussettes");
        assertThat(order.getQuantity()).isEqualTo(10);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        var updatedOrderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(repository).persist(updatedOrderCaptor.capture());
        assertThat(updatedOrderCaptor.getValue()).isNotNull();
        assertThat(updatedOrderCaptor.getValue()).isNotSameAs(existing);
        assertThat(updatedOrderCaptor.getValue().getId()).isEqualTo("123");
        assertThat(updatedOrderCaptor.getValue().getProductId()).isEqualTo("chaussettes");
        assertThat(updatedOrderCaptor.getValue().getQuantity()).isEqualTo(10);
        assertThat(updatedOrderCaptor.getValue().getStatus()).isEqualTo(OrderStatus.CANCELLED);

        verify(eventPublisher).publishCancelledEvent(order);
    }

    @Test
    @DisplayName("cancel an executed order is not permit")
    void updateShouldThrowException() {
        // given
        Order existing = Order.builder()
                .id("123")
                .productId("chaussettes")
                .quantity(10)
                .status(OrderStatus.EXECUTED)
                .build();

        when(repository.findById("123")).thenReturn(Uni.createFrom().item(existing));

        // when
        var exception = service.cancel("123")
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertFailed()
                .getFailure();

        // then
        assertThat(exception)
                .isInstanceOf(NotCancellableException.class)
                .asInstanceOf(InstanceOfAssertFactories.type(NotCancellableException.class))
                .satisfies(e -> assertThat(e.getId()).isEqualTo("123"));
        verify(repository, never()).persist(any());
    }
}
