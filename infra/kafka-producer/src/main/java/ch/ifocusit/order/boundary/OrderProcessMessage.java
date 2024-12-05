package ch.ifocusit.order.boundary;

import java.time.Instant;
import ch.ifocusit.order.domain.model.Order;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

@Builder
@Getter
public class OrderProcessMessage {
    private Order order;
    private EventType eventType;

    @Default
    private Instant timestamp = Instant.now();
}
