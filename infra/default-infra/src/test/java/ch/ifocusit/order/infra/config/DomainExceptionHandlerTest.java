package ch.ifocusit.order.infra.config;

import static io.restassured.RestAssured.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ch.ifocusit.order.domain.model.exception.QuantityException;
import ch.ifocusit.order.infra.config.helper.GreetingService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.ext.ExceptionMapper;

@QuarkusTest
public class DomainExceptionHandlerTest {

    @InjectMock
    GreetingService service;

    @Inject
    ExceptionMapper<QuantityException> quantityException;

    @Test
    void quantityException() {
        assertThat(quantityException).isNotNull();

        Mockito.when(service.sayHello()).thenThrow(new QuantityException(1, 2));

        given().when().get("/exception-test").then()
                .statusCode(422)
                .statusLine("Quantity 1 must be greater than 2");
    }
}
