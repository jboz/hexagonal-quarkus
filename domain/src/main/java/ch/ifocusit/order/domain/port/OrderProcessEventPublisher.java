package ch.ifocusit.order.domain.port;

import ch.ifocusit.order.domain.model.Order;

public interface OrderProcessEventPublisher {

    void publishExecutedEvent(Order order);

    void publishCancelledEvent(Order order);
}
