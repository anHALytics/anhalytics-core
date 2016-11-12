package fr.inria.anhalytics.commons.exceptions;

import java.io.IOException;

/**
 * Created by lfoppiano on 14/06/16.
 *
 * This class represent general problems not related to content. For content problem check #DataException
 */
public class SystemException extends RuntimeException {

    public SystemException() {
        super();
    }

    public SystemException(String message) {
        super(message);
    }

    public SystemException(Throwable cause) {
        super(cause);
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }
}
