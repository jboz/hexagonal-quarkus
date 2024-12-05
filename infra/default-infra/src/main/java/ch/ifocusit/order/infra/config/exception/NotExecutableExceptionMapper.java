package ch.ifocusit.order.infra.config.exception;

import ch.ifocusit.order.domain.model.exception.NotExecutableException;
import io.quarkiverse.resteasy.problem.HttpProblem;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class NotExecutableExceptionMapper implements ExceptionMapper<NotExecutableException> {

    @Override
    public Response toResponse(NotExecutableException e) {
        return HttpProblem.builder()
                .withStatus(422)
                .withTitle("Order " + e.getId() + " is not executable !")
                .build()
                .toResponse();
    }
}
