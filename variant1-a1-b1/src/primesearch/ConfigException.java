package primesearch;

/**
 * Thrown when configuration file is missing or has invalid values
 *
 * @author Akaraan
 */
public final class ConfigException extends Exception {
    private static final long serialVersionUID = 1L;

    public ConfigException(String message) {
        super(message);
    }

    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
