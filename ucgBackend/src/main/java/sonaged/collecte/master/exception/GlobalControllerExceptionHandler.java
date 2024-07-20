package sonaged.collecte.master.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;




@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerExceptionHandler {

    private final ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Error invalidInput(MethodArgumentNotValidException e) {

        logger.error(e.getMessage());

        Error error = new Error();
        error.setCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
        error.setMessage("unable to process the contained instructions");
        error.setErrors(e.getBindingResult().getFieldErrors().stream().map(objectError -> objectError.getField() + " " + objectError.getDefaultMessage()).collect(Collectors.toList()));

        return error;
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    public Error notFound(ResourceNotFoundException e) {

        logger.error(e.getMessage());

        Error error = new Error();
        error.setCode(HttpStatus.NOT_FOUND.value());
        error.setMessage(e.getMessage());

        return error;
    }



    @ResponseStatus(value = HttpStatus.CONFLICT)
    @ExceptionHandler(ResourceAlreadyExistException.class)
    public Error conflict(ResourceAlreadyExistException e) {

        logger.error(e.getMessage());

        Error error = new Error();
        error.setCode(HttpStatus.CONFLICT.value());
        error.setMessage(e.getMessage());

        return error;
    }



    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Error invalidInput(final HttpMessageNotReadableException e) {

        logger.error("HttpMessageNotReadableException: {}", e.getMessage());

        Throwable rootCause = e;
        while(rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }

        final Error error = new Error();
        error.setCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
        error.setMessage(rootCause.getMessage());

        return error;
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public Error internalError(Exception e) {

        logger.error("InternalError", e);

        Error error = new Error();
        error.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setMessage("Error : " + e.getMessage());

        return error;
    }

    private Error decode(final String data) {

        try {
            return objectMapper.readValue(data, Error.class);
        } catch (final IOException e) {
            logger.warn("Cannot decode error received from distant server.", e);
            return null;
        }
    }

    static class Error {

        private Integer code;
        private String message;
        private List<String> errors;
        private Instant timestamp;
        private  String description;

        Error() {
        }

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public List<String> getErrors() {
            return errors;
        }

        public void setErrors(List<String> errors) {
            this.errors = errors;
        }

        public Instant getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Instant timestamp) {
            this.timestamp = timestamp;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
