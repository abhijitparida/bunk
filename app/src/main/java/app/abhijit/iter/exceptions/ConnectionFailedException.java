package app.abhijit.iter.exceptions;

/**
 * This exception is raised when the api server is unreachable.
 */
public class ConnectionFailedException extends RuntimeException {

    public ConnectionFailedException() {
        super("Could not connect to ITER servers");
    }
}
