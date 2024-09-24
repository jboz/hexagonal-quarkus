package ch.ifocusit.order.model.events;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NewOrderEvent {
    String productId;
    int quantity;
}
