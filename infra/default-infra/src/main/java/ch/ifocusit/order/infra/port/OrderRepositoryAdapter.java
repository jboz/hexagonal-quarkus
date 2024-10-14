package ch.ifocusit.order.infra.port;

import org.bson.types.ObjectId;
import ch.ifocusit.order.infra.entity.OrderEntity;
import ch.ifocusit.order.model.Order;
import ch.ifocusit.order.port.OrderRepository;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OrderRepositoryAdapter implements OrderRepository {

    @Override
    public Uni<Order> persist(Order order) {
        return OrderEntity.toEntity(order)
                .<OrderEntity>persistOrUpdate()
                .onItem().transform(OrderEntity::toDomain);
    }

    @Override
    public Multi<Order> all() {
        return OrderEntity.<OrderEntity>streamAll()
                .onItem().transform(OrderEntity::toDomain);
    }

    @Override
    public Uni<Order> findById(String id) {
        return OrderEntity.<OrderEntity>findById(new ObjectId(id))
                .onItem().ifNotNull().transform(OrderEntity::toDomain);
    }
}
