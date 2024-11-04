package ch.ifocusit.order.model;

import io.quarkiverse.resteasy.problem.HttpProblem;
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
            throw HttpProblem.builder()
                    .withStatus(422)
                    .withTitle("Quantity must be greater than previous one: " + quantity)
                    .build();
        }
        return Order.builder()
                .productId(productId)
                .quantity(newQuantity - quantity)
                .build()
                .validate();
    }

    public Order validate() {
        if (quantity < 1) {
            throw HttpProblem.builder()
                    .withStatus(422)
                    .withTitle("Quantity must be greater than zero")
                    .build();
        }
        return this;
    }
}
