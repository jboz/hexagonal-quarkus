package ch.ifocusit.order.boundary;

import static org.assertj.core.api.Assertions.*;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ch.ifocusit.order.domain.model.Order;
import ch.ifocusit.order.domain.model.OrderStatus;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import io.quarkus.kafka.client.serialization.ObjectMapperSerializer;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.ConsumerTask;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import jakarta.inject.Inject;

@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
public class OrderProcessPublisherTest {

    @InjectKafkaCompanion
    KafkaCompanion companion;

    @Inject
    OrderProcessPublisher publisher;

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
    void executedEvent() {
        var order = Order.builder()
                .id("123")
                .productId("chaussette")
                .quantity(2)
                .status(OrderStatus.EXECUTED)
                .build();
        var start = Instant.now();

        publisher.publishOrderExecutedEvent(order);

        assertPublished(order, EventType.EXECUTED, start);
    }

    @Test
    void updatedEvent() {
        var order = Order.builder()
                .id("123")
                .productId("chaussette")
                .quantity(2)
                .status(OrderStatus.EXECUTED)
                .build();
        var start = Instant.now();

        publisher.publishOrderUpdatedEvent(order);

        assertPublished(order, EventType.UPDATED, start);
    }

    void assertPublished(Order order, EventType eventType, Instant start) {
        ConsumerTask<String, OrderProcessMessage> records = companion
                .consume(OrderProcessMessage.class)
                .fromTopics("orders-processed", 1)
                .awaitCompletion();

        assertThat(records).hasSize(1)
                .first().satisfies(record -> {
                    assertThat(record.key()).isEqualTo(order.getId());
                    assertThat(record.value()).isNotNull();
                    assertThat(record.value().getOrder()).isEqualTo(order);
                    assertThat(record.value().getEventType()).isEqualTo(eventType);
                    assertThat(record.value().getTimestamp()).isBetween(start, Instant.now());
                });
    }
}
