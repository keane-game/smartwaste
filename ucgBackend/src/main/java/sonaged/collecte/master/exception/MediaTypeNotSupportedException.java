package sonaged.collecte.master.exception;

import java.io.Serial;

public class MediaTypeNotSupportedException extends RuntimeException{

    @Serial
    private static final long serialVersionUID = 1L;

    public MediaTypeNotSupportedException(final String message) {
        super(message);
    }
    public MediaTypeNotSupportedException(String entity, Object value) {
        super(entity + " Me: " + value);
    }
}
