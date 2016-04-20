package fr.inria.anhalytics.commons.exceptions;

import java.io.IOException;

/**
 *
 * @author achraf
 */
public class UnreachableAnnotateServiceException extends IOException{
    public UnreachableAnnotateServiceException() {
        super();
    }

    public UnreachableAnnotateServiceException(String message) {
        super(message);
    }

    public UnreachableAnnotateServiceException(Throwable cause) {
        super(cause);
    }

    public UnreachableAnnotateServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
