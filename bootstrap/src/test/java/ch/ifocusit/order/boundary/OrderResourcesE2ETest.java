package ch.ifocusit.order.boundary;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.*;
import static org.assertj.core.api.Assertions.*;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.github.tomakehurst.wiremock.client.WireMock;
import ch.ifocusit.order.boundary.dto.EventType;
import ch.ifocusit.order.boundary.dto.OrderProcessMessage;
import ch.ifocusit.order.domain.model.Order;
import ch.ifocusit.order.domain.model.OrderStatus;
import ch.ifocusit.order.infra.entity.OrderEntity;
import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import io.quarkus.kafka.client.serialization.ObjectMapperSerializer;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.smallrye.reactive.messaging.kafka.companion.ConsumerTask;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import jakarta.ws.rs.core.MediaType;

@QuarkusTest
@ConnectWireMock
@QuarkusTestResource(KafkaCompanionResource.class)
public class OrderResourcesE2ETest {

    @InjectKafkaCompanion
    KafkaCompanion companion;

    WireMock wireMock;

    @BeforeEach
    public void setUp() {
        companion.registerSerde(
                OrderProcessMessage.class,
                new ObjectMapperSerializer<>(),
                new ObjectMapperDeserializer<>(OrderProcessMessage.class));
    }

    @AfterEach
    public void tearDown() {
        companion.topics().clear("orders-processed");
    }

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
    @RunOnVertxContext
    public void createOrder() {
        var start = Instant.now();

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
        assertThat(created.getStatus()).isEqualTo(OrderStatus.EXECUTED);
        assertThat(created.getProductId()).isEqualTo("chaussettes");

        wireMock.verifyThat(1,
                anyRequestedFor(urlEqualTo("/product-store/api/available?productId=chaussettes&quantity=5")));

        ConsumerTask<String, OrderProcessMessage> records = companion
                .consume(OrderProcessMessage.class)
                .fromTopics("orders-processed", 1)
                .awaitCompletion();

        assertThat(records).hasSize(1)
                .first().satisfies(record -> {
                    assertThat(record.key()).isEqualTo(created.getId());
                    assertThat(record.value()).isNotNull();
                    assertThat(record.value().getOrder()).isEqualTo(created);
                    assertThat(record.value().getEventType()).isEqualTo(EventType.EXECUTED);
                    assertThat(record.value().getTimestamp()).isBetween(start, Instant.now());
                });
    }

    @Test
    @RunOnVertxContext
    public void cancelOrder(UniAsserter asserter) {
        var entity = new OrderEntity();
        entity.setStatus(OrderStatus.NEW);
        entity.setProductId("chaussettes");
        entity.setQuantity(10);
        asserter.execute(
                () -> entity.<OrderEntity>persist().onItem()
                        .invoke(saved -> asserter.putData("id", saved.id.toHexString())));

        asserter.execute(() -> {
            var updated = given()
                    .when()
                    .contentType(MediaType.APPLICATION_JSON)
                    .put("/api/orders/" + asserter.getData("id") + "/cancellation")
                    .then()
                    .statusCode(200)
                    .extract().as(Order.class);

            assertThat(updated.getQuantity()).isEqualTo(10);
            assertThat(updated.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(updated.getProductId()).isEqualTo(entity.productId);
        });
    }
}
