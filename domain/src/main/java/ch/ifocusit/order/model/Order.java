package ch.ifocusit.order.model;

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
        // create a new order with the difference
        if (newQuantity <= quantity) {
            throw new IllegalArgumentException("Quantity must be greater than previous one: " + quantity);
        }
        return Order.builder()
                .productId(productId)
                .quantity(newQuantity - quantity)
                .build()
                .validate();
    }

    public Order validate() {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        return this;
    }
}
