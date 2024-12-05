package ch.ifocusit.order.domain.model.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class QuantityException extends RuntimeException {
    int quantity;
    int referenceQuantity;
}
