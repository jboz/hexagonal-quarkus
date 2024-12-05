package ch.ifocusit.order.domain.model.events;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@EqualsAndHashCode
public class NewOrderEvent {
    String productId;
    int quantity;
}
