package fr.inria.anhalytics.index.exceptions;

import fr.inria.anhalytics.commons.exceptions.ServiceException;

/**
 *
 * @author azhar
 */
public class IndexingServiceException extends ServiceException {
    private static final long serialVersionUID = -3337770841815682150L;

    public IndexingServiceException() {
        super();
    }

    public IndexingServiceException(String message) {
        super(message);
    }

    public IndexingServiceException(Throwable cause) {
        super(cause);
    }

    public IndexingServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
