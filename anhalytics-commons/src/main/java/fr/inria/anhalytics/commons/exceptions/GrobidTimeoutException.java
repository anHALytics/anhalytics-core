/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.anhalytics.commons.exceptions;

/**
 *
 * @author azhar
 */
public class GrobidTimeoutException extends RuntimeException {

    private static final long serialVersionUID = -3337770841815682150L;

    public GrobidTimeoutException() {
        super();
    }

    public GrobidTimeoutException(String message) {
        super(message);
    }

    public GrobidTimeoutException(Throwable cause) {
        super(cause);
    }

    public GrobidTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
