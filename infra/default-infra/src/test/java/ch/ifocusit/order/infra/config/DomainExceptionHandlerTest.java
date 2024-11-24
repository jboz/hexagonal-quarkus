package ch.ifocusit.order.infra.config;

import static io.restassured.RestAssured.*;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test;
import ch.ifocusit.order.model.exception.QuantityException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@QuarkusTest
public class DomainExceptionHandlerTest {

    @InjectMock
    GreetingService service;

    @Test
    void quantityException() {
        when(service.sayHello()).thenThrow(new QuantityException(1, 2));

        given().when().get("/test").then()
                .statusCode(422)
                .statusLine("toto");
    }

    @ApplicationScoped
    public static class GreetingService {
        public String sayHello() {
            return "Hello World!";
        }
    }

    @Path("/test")
    public static class GreetingResource {

        @Inject
        GreetingService service;

        @GET
        public String sayHello() {
            return service.sayHello();
        }
    }
}
