package ch.ifocusit.order.infra.port.productstore;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@RegisterRestClient(configKey = "product-store")
public interface ProductStoreRestClient {

    @GET
    @Path("/available")
    Uni<Void> ifAvailable(@QueryParam("productId") String productId, @QueryParam("quantity") int quantity);

}
