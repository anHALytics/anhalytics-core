package fr.inria.anhalytics.annotate.exceptions;

/**
 *
 * @author patrice
 */
public class AnnotatorNotAvailableException extends RuntimeException {

    public AnnotatorNotAvailableException() {
        super();
    }

    public AnnotatorNotAvailableException(String message) {
        super(message);
    }

    public AnnotatorNotAvailableException(Throwable cause) {
        super(cause);
    }

    public AnnotatorNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}