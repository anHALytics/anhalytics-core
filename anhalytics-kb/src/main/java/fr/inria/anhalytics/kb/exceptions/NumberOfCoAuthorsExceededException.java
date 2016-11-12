
package fr.inria.anhalytics.kb.exceptions;

/**
 *
 * @author azhar
 */
public class NumberOfCoAuthorsExceededException 
    extends Exception{
    public NumberOfCoAuthorsExceededException() {
        super();
    }

    public NumberOfCoAuthorsExceededException(String message) {
        super(message);
    }

    public NumberOfCoAuthorsExceededException(Throwable cause) {
        super(cause);
    }

    public NumberOfCoAuthorsExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
