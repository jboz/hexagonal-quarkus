package ch.ifocusit.order.config;

import ch.ifocusit.order.port.OrderRepository;
import ch.ifocusit.order.port.ProductStore;
import ch.ifocusit.order.service.OrderService;
import jakarta.enterprise.inject.Produces;

public class DomainBeansProvider {

    @Produces
    public OrderService orderService(OrderRepository orderStoreService, ProductStore productStore) {
        return new OrderService(orderStoreService, productStore);
    }
}
