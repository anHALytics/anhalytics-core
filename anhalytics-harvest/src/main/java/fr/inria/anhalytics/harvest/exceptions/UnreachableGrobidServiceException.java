package fr.inria.anhalytics.harvest.exceptions;

import fr.inria.anhalytics.commons.exceptions.SystemException;
import java.io.IOException;

/**
 *
 * @author achraf
 */
public class UnreachableGrobidServiceException extends SystemException {

    public UnreachableGrobidServiceException(int responseCode) {
        super("Grobid service is not alive. HTTP error: " + responseCode);
    }

    public UnreachableGrobidServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
