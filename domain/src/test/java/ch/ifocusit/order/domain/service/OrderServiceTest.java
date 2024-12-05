package ch.ifocusit.order.domain.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
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
        when(productStore.ifAvailable("chaussettes", 100))
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
        assertThat(order.getStatus()).isEqualTo(OrderStatus.NEW);
    }

    @Test
    @DisplayName("updated an executed order should create a new order with the quantity difference")
    void updateShouldCreateAnewOne() {
        // given
        Order existing = Order.builder()
                .id("123")
                .productId("chaussettes")
                .quantity(10)
                .status(OrderStatus.EXECUTED)
                .build();

        when(repository.findById("123")).thenReturn(Uni.createFrom().item(existing));
        when(repository.persist(any())).thenAnswer((invocation) -> {
            Order order = invocation.getArgument(0);
            return Uni.createFrom().item(order.toBuilder().id("456").build());
        });

        // when
        var order = service.update("123", 15)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                // then
                .assertCompleted()
                .getItem();

        assertThat(order).isNotNull();
        assertThat(order.getId()).isEqualTo("456");
        assertThat(order.getProductId()).isEqualTo("chaussettes");
        assertThat(order.getQuantity()).isEqualTo(5);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.NEW);

        var updatedOrderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(repository).persist(updatedOrderCaptor.capture());
        assertThat(updatedOrderCaptor.getValue()).isNotNull();
        assertThat(updatedOrderCaptor.getValue()).isNotSameAs(existing);
        assertThat(updatedOrderCaptor.getValue().getId()).isNull();
        assertThat(updatedOrderCaptor.getValue().getProductId()).isEqualTo("chaussettes");
        assertThat(updatedOrderCaptor.getValue().getQuantity()).isEqualTo(5);
        assertThat(updatedOrderCaptor.getValue().getStatus()).isEqualTo(OrderStatus.NEW);
    }

    @Test
    @DisplayName("updated an new order should update the existing one without creating a new one")
    void updateShouldNotCreateAnewOne() {
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
            return Uni.createFrom().item(order);
        });

        // when
        var order = service.update("123", 15)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        // then
        assertThat(order).isNotNull();
        assertThat(order.getId()).isEqualTo("123");
        assertThat(order.getProductId()).isEqualTo("chaussettes");
        assertThat(order.getQuantity()).isEqualTo(15);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.NEW);

        var updatedOrderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(repository).persist(updatedOrderCaptor.capture());
        assertThat(updatedOrderCaptor.getValue()).isNotNull();
        assertThat(updatedOrderCaptor.getValue()).isNotSameAs(existing);
        assertThat(updatedOrderCaptor.getValue().getId()).isEqualTo("123");
        assertThat(updatedOrderCaptor.getValue().getProductId()).isEqualTo("chaussettes");
        assertThat(updatedOrderCaptor.getValue().getQuantity()).isEqualTo(15);
        assertThat(updatedOrderCaptor.getValue().getStatus()).isEqualTo(OrderStatus.NEW);
    }
}
