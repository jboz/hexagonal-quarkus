package ch.ifocusit.order.boundary;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import ch.ifocusit.order.domain.model.events.NewOrderEvent;
import ch.ifocusit.order.domain.service.OrderService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class OrderConsumer {
    public static final String ORDERS_TOPIC = "orders";

    @Inject
    OrderService orderService;

    @Incoming(ORDERS_TOPIC)
    public void consume(NewOrderEvent event) {
        orderService.execute(event);
    }
}
