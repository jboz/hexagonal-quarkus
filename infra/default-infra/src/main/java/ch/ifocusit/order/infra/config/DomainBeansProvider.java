package ch.ifocusit.order.infra.config;

import ch.ifocusit.order.domain.port.OrderRepository;
import ch.ifocusit.order.domain.port.ProductStore;
import ch.ifocusit.order.domain.service.OrderService;
import jakarta.enterprise.inject.Produces;

public class DomainBeansProvider {

    @Produces
    public OrderService orderService(OrderRepository orderStoreService, ProductStore productStore) {
        return new OrderService(orderStoreService, productStore);
    }
}
