package ch.ifocusit.order.infra;

import static org.mockito.Mockito.*;
import ch.ifocusit.order.domain.port.OrderProcessEventPublisher;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

public class MockConfiguration {

    @Produces
    @ApplicationScoped
    OrderProcessEventPublisher eventPublisher() {
        return mock(OrderProcessEventPublisher.class);
    }
}
