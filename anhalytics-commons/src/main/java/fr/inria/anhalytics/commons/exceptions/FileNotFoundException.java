package fr.inria.anhalytics.commons.exceptions;

/**
 *
 * @author achraf
 */
public class FileNotFoundException extends Exception{
    public FileNotFoundException() {
        super();
    }

    public FileNotFoundException(String message) {
        super(message);
    }

    public FileNotFoundException(Throwable cause) {
        super(cause);
    }

    public FileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
