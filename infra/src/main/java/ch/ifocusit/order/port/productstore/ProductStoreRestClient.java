package ch.ifocusit.order.port.productstore;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@RegisterRestClient(configKey = "product-store")
public interface ProductStoreRestClient {

    @GET
    @Path("/availability")
    Uni<Void> isAvailable(@QueryParam("productId") String productId, @QueryParam("quantity") int quantity);

}
