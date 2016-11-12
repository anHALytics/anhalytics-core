package fr.inria.anhalytics.commons.exceptions;

/**
 * Created by lfoppiano on 12/10/16.
 *
 * This class represent problems due to external services, third-party libraries, databases access.
 */
public class ServiceException extends RuntimeException {

    public ServiceException() {
        super();
    }

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(Throwable cause) {
        super(cause);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}