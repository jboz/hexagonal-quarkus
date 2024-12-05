package ch.ifocusit.order.domain.model.exception;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NotExecutableException extends RuntimeException {
    String id;
}
