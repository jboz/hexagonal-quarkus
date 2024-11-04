package ch.ifocusit.order.boundary;

import static ch.ifocusit.order.boundary.OrderConsumer.*;
import static org.awaitility.Awaitility.*;
import static org.mockito.Mockito.*;
import java.time.Duration;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ch.ifocusit.order.model.events.NewOrderEvent;
import ch.ifocusit.order.service.OrderService;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import io.quarkus.kafka.client.serialization.ObjectMapperSerializer;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
public class OrderConsumerTest {

    @InjectKafkaCompanion
    KafkaCompanion companion;

    @InjectMock
    OrderService orderService;

    @BeforeEach
    public void setUp() {
        companion.registerSerde(
                NewOrderEvent.class,
                new ObjectMapperSerializer<>(),
                new ObjectMapperDeserializer<>(NewOrderEvent.class));
    }

    @Test
    void testConsume() {
        NewOrderEvent event = NewOrderEvent.builder().productId("chaussettes").quantity(2).build();

        companion
                .produce(NewOrderEvent.class)
                .fromRecords(new ProducerRecord<>(ORDERS_TOPIC, event));

        await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> verify(orderService).execute(event));
    }

    @Produces
    @ApplicationScoped
    public OrderService orderService() {
        return mock(OrderService.class);
    }
}
