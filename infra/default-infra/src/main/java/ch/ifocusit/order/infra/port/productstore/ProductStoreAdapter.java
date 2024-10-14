package ch.ifocusit.order.infra.port.productstore;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import ch.ifocusit.order.port.ProductStore;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ProductStoreAdapter implements ProductStore {

    @Inject
    @RestClient
    ProductStoreRestClient client;

    @Override
    public Uni<Void> ifAvailable(String productId, int quantity) {
        return client.ifAvailable(productId, quantity);
    }
}
