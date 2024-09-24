package ch.ifocusit.order.port;

import io.smallrye.mutiny.Uni;

public interface ProductStore {
    Uni<Void> isAvailable(String productId, int quantity);
}
