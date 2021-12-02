package ch.admin.bag.covidcertificate.api.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class ResponseStatusExceptionHandler {

    @ExceptionHandler(value = {InputValidationException.class})
    protected ResponseEntity<Object> handleCreateCertificateException(InputValidationException ex) {
            log.warn(ex.getMessage()+"{ "+ex.getError().toString()+"}", ex);
            return new ResponseEntity<>(ex.getError(), HttpStatus.BAD_REQUEST);
    }
}
