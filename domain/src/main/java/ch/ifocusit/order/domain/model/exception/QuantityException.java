package ch.ifocusit.order.domain.model.exception;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class QuantityException extends RuntimeException {
    int quantity;
    int referenceQuantity;
}
