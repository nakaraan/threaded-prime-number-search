package primesearch;

/**
 * Exception handler for invalid inputs and undesirable states
 *
 * @author Akaraan
 */
public class ConfigException extends Exception {
    private static final long serialVersionUID = 1L;

    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
