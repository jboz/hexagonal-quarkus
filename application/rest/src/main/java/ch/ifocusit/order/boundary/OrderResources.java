package ch.ifocusit.order.boundary;

import ch.ifocusit.order.domain.model.Order;
import ch.ifocusit.order.domain.model.events.NewOrderEvent;
import ch.ifocusit.order.domain.service.OrderService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/orders")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OrderResources {

    @Inject
    OrderService orderService;

    @GET
    public Multi<Order> orders() {
        return orderService.orders();
    }

    @POST
    public Uni<Order> execute(@NotNull @Valid NewOrderEvent event) {
        return orderService.execute(event);
    }

    @PATCH
    @Path("/{id}")
    public Uni<Order> update(@PathParam("id") String id, @NotNull @QueryParam("quantity") Integer quantity) {
        return orderService.update(id, quantity);
    }
}
