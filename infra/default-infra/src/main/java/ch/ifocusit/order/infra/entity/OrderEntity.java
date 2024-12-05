package ch.ifocusit.order.infra.entity;

import java.util.Optional;
import org.bson.types.ObjectId;
import ch.ifocusit.order.domain.model.Order;
import ch.ifocusit.order.domain.model.OrderStatus;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntity;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Setter
@MongoEntity
public class OrderEntity extends ReactivePanacheMongoEntity {
    public OrderStatus status;
    public String productId;
    public int quantity;

    public Order toDomain() {
        return Order.builder()
                .id(id.toHexString())
                .status(status)
                .productId(productId)
                .quantity(quantity)
                .build();
    }

    public static OrderEntity toEntity(Order order) {
        var entity = new OrderEntity();
        entity.id = Optional.ofNullable(order.getId()).map(ObjectId::new).orElse(null);
        entity.status = order.getStatus();
        entity.productId = order.getProductId();
        entity.quantity = order.getQuantity();
        return entity;
    }
}
