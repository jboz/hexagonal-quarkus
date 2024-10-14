package ch.ifocusit.order.port;

import io.smallrye.mutiny.Uni;

public interface ProductStore {
    Uni<Void> ifAvailable(String productId, int quantity);
}
