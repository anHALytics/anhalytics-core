package fr.inria.anhalytics.annotate.exceptions;

import fr.inria.anhalytics.commons.exceptions.ServiceException;

/**
 *
 * @author patrice
 */
public class AnnotatorNotAvailableException extends ServiceException {

    public AnnotatorNotAvailableException(String message) {
        super(message);
    }

}
