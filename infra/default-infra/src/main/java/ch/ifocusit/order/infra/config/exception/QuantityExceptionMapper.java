package ch.ifocusit.order.infra.config.exception;

import ch.ifocusit.order.domain.model.exception.QuantityException;
import io.quarkiverse.resteasy.problem.HttpProblem;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class QuantityExceptionMapper implements ExceptionMapper<QuantityException> {

    @Override
    public Response toResponse(QuantityException e) {
        return HttpProblem.builder()
                .withStatus(422)
                .withTitle("Quantity " + e.getQuantity() + " must be greater than " + e.getReferenceQuantity() + " !")
                .build()
                .toResponse();
    }
}
