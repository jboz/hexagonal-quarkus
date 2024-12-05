package ch.ifocusit.order.infra.config.exception;

import ch.ifocusit.order.domain.model.exception.NotCancellableException;
import io.quarkiverse.resteasy.problem.HttpProblem;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class NotCancellableExceptionMapper implements ExceptionMapper<NotCancellableException> {

    @Override
    public Response toResponse(NotCancellableException e) {
        return HttpProblem.builder()
                .withStatus(422)
                .withTitle("Order " + e.getId() + " is not cancellable !")
                .build()
                .toResponse();
    }
}
