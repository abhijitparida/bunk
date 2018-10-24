package app.abhijit.iter.exceptions;

/**
 * This exception is raised when the server returns an invalid response.
 */
public class InvalidResponseException extends RuntimeException {

    public InvalidResponseException() {
        super("Invalid API response");
    }
}
