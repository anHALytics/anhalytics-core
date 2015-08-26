package fr.inria.anhalytics.commons.exceptions;

/**
 *
 * @author achraf
 */
public class PropertyException extends RuntimeException {

    private static final long serialVersionUID = -3337770841815682150L;

    public PropertyException() {
        super();
    }

    public PropertyException(String message) {
        super(message);
    }

    public PropertyException(Throwable cause) {
        super(cause);
    }

    public PropertyException(String message, Throwable cause) {
        super(message, cause);
    }
}
