package ch.ifocusit.order.boundary;

import static io.restassured.RestAssured.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test;
import ch.ifocusit.order.model.Order;
import ch.ifocusit.order.model.OrderStatus;
import ch.ifocusit.order.service.OrderService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Multi;
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

    @Produces
    @ApplicationScoped
    public OrderService orderService() {
        return mock(OrderService.class);
    }
}
