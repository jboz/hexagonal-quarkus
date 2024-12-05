package ch.ifocusit.order.infra.config.exception;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ch.ifocusit.order.domain.model.exception.NotCancellableException;
import ch.ifocusit.order.domain.model.exception.NotExecutableException;
import ch.ifocusit.order.domain.model.exception.QuantityException;
import ch.ifocusit.order.infra.config.helper.GreetingService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ExceptionMapperTest {

    @InjectMock
    GreetingService service;

    @Test
    void notCancellableException() {
        Mockito.when(service.sayHello()).thenThrow(NotCancellableException.builder().id("123").build());

        given().when().get("/exception-test").then()
                .statusCode(422)
                .body("status", equalTo(422))
                .body("title", equalTo("Order 123 is not cancellable !"));
    }

    @Test
    void notExcecutableException() {
        Mockito.when(service.sayHello()).thenThrow(NotExecutableException.builder().id("123").build());

        given().when().get("/exception-test").then()
                .statusCode(422)
                .body("status", equalTo(422))
                .body("title", equalTo("Order 123 is not executable !"));
    }

    @Test
    void quantityException() {
        Mockito.when(service.sayHello()).thenThrow(QuantityException.builder().quantity(1).referenceQuantity(2).build());

        given().when().get("/exception-test").then()
                .statusCode(422)
                .body("status", equalTo(422))
                .body("title", equalTo("Quantity 1 must be greater than 2 !"));
    }
}
