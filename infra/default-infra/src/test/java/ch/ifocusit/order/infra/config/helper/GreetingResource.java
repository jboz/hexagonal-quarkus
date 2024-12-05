package ch.ifocusit.order.infra.config.helper;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/exception-test")
public class GreetingResource {

    @Inject
    GreetingService service;

    @GET
    public String sayHello() {
        return service.sayHello();
    }
}
