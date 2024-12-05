package ch.ifocusit.order.domain.port;

import ch.ifocusit.order.domain.model.Order;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface OrderRepository {
    Uni<Order> persist(Order order);

    Multi<Order> all();

    Uni<Order> findById(String id);
}
