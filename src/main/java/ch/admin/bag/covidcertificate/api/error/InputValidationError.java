package ch.admin.bag.covidcertificate.api.error;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public class InputValidationError implements Serializable {
    @JsonIgnore
    private final Class<?> clazz;
    private final String field;
    private final String errorMessage;

    @Override
    public String toString() {
        return "InputValidationError{" +
                "class=" + clazz +
                ", field='" + field + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
