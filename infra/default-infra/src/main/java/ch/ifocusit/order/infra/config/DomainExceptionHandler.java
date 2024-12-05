package ch.ifocusit.order.infra.config;

import ch.ifocusit.order.domain.model.exception.QuantityException;
import io.quarkiverse.resteasy.problem.HttpProblem;
import jakarta.enterprise.inject.Produces;
import jakarta.ws.rs.ext.ExceptionMapper;

public class DomainExceptionHandler {

    @Produces
    public ExceptionMapper<QuantityException> quantityException() {
        return (e) -> {
            throw HttpProblem.builder()
                    .withStatus(422)
                    .withTitle("Quantity " + e.getQuantity() + " must be greater than " + e.getReferenceQuantity())
                    .build();
        };
    }
}
