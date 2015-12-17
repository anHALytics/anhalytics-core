package fr.inria.anhalytics.commons.exceptions;

/**
 *
 * @author achraf
 */
public class ElasticSearchConfigurationException extends RuntimeException {
    private static final long serialVersionUID = -3337770841815682150L;

    public ElasticSearchConfigurationException() {
        super();
    }

    public ElasticSearchConfigurationException(String message) {
        super(message);
    }

    public ElasticSearchConfigurationException(Throwable cause) {
        super(cause);
    }

    public ElasticSearchConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
