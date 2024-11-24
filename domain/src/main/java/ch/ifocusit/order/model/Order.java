package ch.ifocusit.order.model;

import ch.ifocusit.order.model.exception.QuantityException;
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

    public Order update(int newQuantity) {
        if (status == OrderStatus.NEW) {
            // update existing
            return toBuilder()
                    .quantity(newQuantity)
                    .build()
                    .validate();
        }
        if (newQuantity < quantity) {
            throw QuantityException.builder().quantity(newQuantity).referenceQuantity(quantity).build();
        }
        // create a new order with the difference
        return Order.builder()
                .productId(productId)
                .quantity(newQuantity - quantity)
                .build()
                .validate();
    }

    public Order validate() {
        if (quantity < 0) {
            throw QuantityException.builder().quantity(quantity).referenceQuantity(0).build();
        }
        return this;
    }
}
