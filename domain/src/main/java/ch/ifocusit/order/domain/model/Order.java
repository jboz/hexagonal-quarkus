package ch.ifocusit.order.domain.model;

import ch.ifocusit.order.domain.model.exception.NotCancellableException;
import ch.ifocusit.order.domain.model.exception.NotExecutableException;
import ch.ifocusit.order.domain.model.exception.QuantityException;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class Order {
    String id;
    @Default
    OrderStatus status = OrderStatus.NEW;
    String productId;
    int quantity;

    public Order cancel() {
        if (status == OrderStatus.EXECUTED) {
            throw NotCancellableException.builder().id(id).build();
        }
        return toBuilder().status(OrderStatus.CANCELLED).build();
    }

    public Order validate() {
        if (quantity < 0) {
            throw QuantityException.builder().quantity(quantity).referenceQuantity(0).build();
        }
        return this;
    }

    public Order execute() {
        if (status != OrderStatus.NEW) {
            throw NotExecutableException.builder().id(id).build();
        }
        return toBuilder().status(OrderStatus.EXECUTED).build();
    }
}
