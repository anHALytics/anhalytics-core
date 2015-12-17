package fr.inria.anhalytics.commons.exceptions;

import java.io.IOException;

/**
 *
 * @author achraf
 */
public class UnreachableNerdServiceException extends IOException{
    public UnreachableNerdServiceException() {
        super();
    }

    public UnreachableNerdServiceException(String message) {
        super(message);
    }

    public UnreachableNerdServiceException(Throwable cause) {
        super(cause);
    }

    public UnreachableNerdServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
