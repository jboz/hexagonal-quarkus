package ch.ifocusit.order.domain.port;

import ch.ifocusit.order.domain.model.Order;

public interface OrderProcessEventPublisher {

    void publishOrderExecutedEvent(Order order);

    void publishOrderUpdatedEvent(Order order);
}
