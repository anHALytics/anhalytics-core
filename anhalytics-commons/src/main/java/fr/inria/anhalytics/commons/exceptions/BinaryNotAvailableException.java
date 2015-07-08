package fr.inria.anhalytics.commons.exceptions;

/**
 *
 * @author Achraf
 */
public class BinaryNotAvailableException extends RuntimeException {

    private static final long serialVersionUID = -3337770841815682150L;

    public BinaryNotAvailableException() {
        super();
    }

    public BinaryNotAvailableException(String message) {
        super(message);
    }

    public BinaryNotAvailableException(Throwable cause) {
        super(cause);
    }

    public BinaryNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}