package ch.ifocusit.order.port;

import ch.ifocusit.order.model.Order;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface OrderRepository {
    Uni<Order> persist(Order order);

    Multi<Order> all();

    Uni<Order> findById(String id);
}
