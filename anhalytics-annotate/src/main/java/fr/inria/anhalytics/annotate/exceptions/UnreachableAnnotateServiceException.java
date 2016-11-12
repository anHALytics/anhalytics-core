package fr.inria.anhalytics.annotate.exceptions;

import fr.inria.anhalytics.commons.exceptions.SystemException;

/**
 *
 * @author achraf
 */
public class UnreachableAnnotateServiceException extends SystemException{
    public UnreachableAnnotateServiceException(int responseCode, String serviceName) {
        super(serviceName+" service is not alive. HTTP error: " + responseCode);
    }

    public UnreachableAnnotateServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
