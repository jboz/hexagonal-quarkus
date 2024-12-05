package ch.ifocusit.order.boundary;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import ch.ifocusit.order.domain.model.Order;
import ch.ifocusit.order.domain.port.OrderProcessEventPublisher;
import io.smallrye.reactive.messaging.kafka.Record;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class OrderProcessPublisher implements OrderProcessEventPublisher {

    @Inject
    @Channel("orders")
    Emitter<Record<String, OrderProcessMessage>> orderEmitter;

    @Override
    public void publishOrderExecutedEvent(Order order) {
        send(order, EventType.EXECUTED);
    }

    @Override
    public void publishOrderUpdatedEvent(Order order) {
        send(order, EventType.UPDATED);
    }

    private void send(Order order, EventType eventType) {
        orderEmitter.send(Record.of(order.getId(), OrderProcessMessage.builder()
                .order(order)
                .eventType(eventType)
                .build()));
    }
}
