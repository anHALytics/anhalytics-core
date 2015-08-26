package fr.inria.anhalytics.commons.exceptions;

/**
 *
 * @author achraf
 */
public class DirectoryNotFoundException extends RuntimeException {

    public DirectoryNotFoundException() {
        super();
    }

    public DirectoryNotFoundException(String message) {
        super(message);
    }

    public DirectoryNotFoundException(Throwable cause) {
        super(cause);
    }

    public DirectoryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
