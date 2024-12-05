package ch.ifocusit.order.boundary;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.*;
import static org.assertj.core.api.Assertions.*;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import ch.ifocusit.order.domain.model.Order;
import ch.ifocusit.order.domain.model.OrderStatus;
import ch.ifocusit.order.infra.entity.OrderEntity;
import ch.ifocusit.quarkus.wiremock.WiremockTestResource;
import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import jakarta.ws.rs.core.MediaType;

@QuarkusIntegrationTest
@ConnectWireMock
public class OrderResourcesTest {

    @Test
    public void getOrders() {
        var entity = new OrderEntity();
        entity.setStatus(OrderStatus.EXECUTED);
        entity.setProductId("chaussettes");
        entity.setQuantity(10);
        entity = entity.<OrderEntity>persist().await().atMost(Duration.ofSeconds(1));

        var orders = given()
                .when().get("/api/orders")
                .then()
                .statusCode(200)
                .extract().as(Order[].class);

        assertThat(orders).hasSizeGreaterThanOrEqualTo(1); // TODO why ? context not cleaned ?
        assertThat(orders).contains(entity.toDomain());
    }

    @Test
    public void createOrder() {
        var created = given()
                .when()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                                "productId": "chaussettes",
                                "quantity": 5
                        }
                        """)
                .post("/api/orders")
                .then()
                .statusCode(200)
                .extract().as(Order.class);

        assertThat(created.getQuantity()).isEqualTo(5);
        assertThat(created.getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(created.getProductId()).isEqualTo("chaussettes");

        WiremockTestResource.wireMockServer
                .verify(1, anyRequestedFor(urlEqualTo("/product-store/api/available?productId=chaussettes&quantity=5")));
    }

    @Test
    @RunOnVertxContext
    public void updateOrder(UniAsserter asserter) {
        var entity = new OrderEntity();
        entity.setStatus(OrderStatus.EXECUTED);
        entity.setProductId("chaussettes");
        entity.setQuantity(10);
        asserter.execute(
                () -> entity.<OrderEntity>persist().onItem()
                        .invoke(saved -> asserter.putData("id", saved.id.toHexString())));

        asserter.execute(() -> {
            var updated = given()
                    .when()
                    .contentType(MediaType.APPLICATION_JSON)
                    .patch("/api/orders/" + asserter.getData("id") + "?quantity=15")
                    .then()
                    .statusCode(200)
                    .extract().as(Order.class);

            assertThat(updated.getQuantity()).isEqualTo(5);
            assertThat(updated.getStatus()).isEqualTo(OrderStatus.NEW);
            assertThat(updated.getProductId()).isEqualTo(entity.productId);
        });
    }
}
