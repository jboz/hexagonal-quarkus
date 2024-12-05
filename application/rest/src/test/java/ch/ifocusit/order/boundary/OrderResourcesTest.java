package ch.ifocusit.order.boundary;

import static io.restassured.RestAssured.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ch.ifocusit.order.domain.model.Order;
import ch.ifocusit.order.domain.model.OrderStatus;
import ch.ifocusit.order.domain.model.events.NewOrderEvent;
import ch.ifocusit.order.domain.service.OrderService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@QuarkusTest
@TestHTTPEndpoint(OrderResources.class)
public class OrderResourcesTest {

    @InjectMock
    OrderService orderService;

    @Test
    void getOrders() {
        when(orderService.orders()).thenReturn(Multi.createFrom()
                .items(
                        Order.builder().id("1").status(OrderStatus.CANCELLED).productId("chaussettes").quantity(10).build(),
                        Order.builder().id("2").status(OrderStatus.EXECUTED).productId("bottes").quantity(6).build()));

        var orders = given()
                .when().get()
                .then()
                .statusCode(200)
                .extract().as(Order[].class);

        assertThat(orders).hasSize(2);
    }

    @Test
    void execute() {
        Order order = Order.builder()
                .id("123")
                .productId("chaussette")
                .quantity(15)
                .build();
        when(orderService.execute(any())).thenReturn(Uni.createFrom().item(order));

        var executed = given()
                .header("Content-Type", "application/json")
                .body("""
                        {
                            "productId": "chaussette",
                            "quantity": 15
                        }
                        """)
                .when().post()
                .then()
                .statusCode(200)
                .extract().as(Order.class);

        assertThat(executed).isEqualTo(order);

        var captor = ArgumentCaptor.forClass(NewOrderEvent.class);
        verify(orderService).execute(captor.capture());
        assertThat(captor.getValue()).isNotNull();
        assertThat(captor.getValue().getProductId()).isEqualTo("chaussette");
        assertThat(captor.getValue().getQuantity()).isEqualTo(15);
    }

    @Test
    void cancel() {
        Order order = Order.builder()
                .id("123")
                .productId("chaussette")
                .quantity(24)
                .build();
        when(orderService.cancel(anyString())).thenReturn(Uni.createFrom().item(order));

        var executed = given()
                .when().put("123/cancellation")
                .then()
                .statusCode(200)
                .extract().as(Order.class);

        assertThat(executed).isEqualTo(order);
        verify(orderService).cancel("123");
    }

    @Produces
    @ApplicationScoped
    public OrderService orderService() {
        return mock(OrderService.class);
    }
}
