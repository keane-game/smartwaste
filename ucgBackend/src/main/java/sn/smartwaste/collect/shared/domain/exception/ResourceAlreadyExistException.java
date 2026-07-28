package sn.smartwaste.collect.shared.domain.exception;

import java.io.Serial;

public class ResourceAlreadyExistException extends RuntimeException{
    @Serial
    private static final long serialVersionUID = 1L;

    public ResourceAlreadyExistException(final String message) {
        super(message);
    }
    public ResourceAlreadyExistException(String entity, Object value) {
        super(entity + " already exit for version with reference : " + value);
    }
}