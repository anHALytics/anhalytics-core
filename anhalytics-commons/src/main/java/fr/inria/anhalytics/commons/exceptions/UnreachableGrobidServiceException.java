package fr.inria.anhalytics.commons.exceptions;

import java.io.IOException;

/**
 *
 * @author achraf
 */
public class UnreachableGrobidServiceException extends IOException{
    public UnreachableGrobidServiceException() {
        super();
    }

    public UnreachableGrobidServiceException(String message) {
        super(message);
    }

    public UnreachableGrobidServiceException(Throwable cause) {
        super(cause);
    }

    public UnreachableGrobidServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
