package ch.admin.bag.covidcertificate.api.error;

import lombok.Getter;
import org.springframework.core.NestedRuntimeException;

@Getter
public class InputValidationException extends NestedRuntimeException {
    private final InputValidationError error;

    public InputValidationException(InputValidationError error) {
        super(error.getErrorMessage());
        this.error = error;
    }
}
