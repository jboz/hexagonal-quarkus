package ch.ifocusit.order.domain.service;

import ch.ifocusit.order.domain.model.Order;
import ch.ifocusit.order.domain.model.events.NewOrderEvent;
import ch.ifocusit.order.domain.port.OrderProcessEventPublisher;
import ch.ifocusit.order.domain.port.OrderRepository;
import ch.ifocusit.order.domain.port.ProductStore;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;
    private final ProductStore productStore;
    private final OrderProcessEventPublisher eventPublisher;

    public Uni<Order> execute(NewOrderEvent event) {
        return productStore.ifAvailable(event.getProductId(), event.getQuantity())
                .map((a) -> Order.builder()
                        .productId(event.getProductId())
                        .quantity(event.getQuantity())
                        .build()
                        .validate()
                        .execute())
                .chain(repository::persist)
                .invoke(eventPublisher::publishExecutedEvent);
    }

    public Multi<Order> orders() {
        return repository.all();
    }

    public Uni<Order> cancel(String id) {
        return repository.findById(id)
                .onItem().ifNotNull().transform((order) -> order.cancel())
                .onItem().ifNotNull().transformToUni(repository::persist)
                .invoke(eventPublisher::publishCancelledEvent);
    }
}
