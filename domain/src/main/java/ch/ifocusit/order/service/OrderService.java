package ch.ifocusit.order.service;

import ch.ifocusit.order.model.Order;
import ch.ifocusit.order.model.events.NewOrderEvent;
import ch.ifocusit.order.port.OrderRepository;
import ch.ifocusit.order.port.ProductStore;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;
    private final ProductStore productStore;

    public Uni<Order> execute(NewOrderEvent event) {
        return productStore.ifAvailable(event.getProductId(), event.getQuantity())
                .map((a) -> Order.builder()
                        .productId(event.getProductId())
                        .quantity(event.getQuantity())
                        .build()
                        .validate())
                .chain(repository::persist);
    }

    public Multi<Order> orders() {
        return repository.all();
    }

    public Uni<Order> update(String id, int quantity) {
        return repository.findById(id)
                .onItem()
                .ifNotNull()
                .transform((order) -> order.update(quantity))
                .onItem()
                .ifNotNull()
                .transformToUni(repository::persist);
    }
}
