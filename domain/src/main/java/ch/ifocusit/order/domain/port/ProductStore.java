package ch.ifocusit.order.domain.port;

import io.smallrye.mutiny.Uni;

public interface ProductStore {
    Uni<Void> ifAvailable(String productId, int quantity);
}
