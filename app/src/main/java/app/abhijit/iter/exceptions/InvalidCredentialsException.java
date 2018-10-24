package app.abhijit.iter.exceptions;

/**
 * This exception is raised when the user's credentials are invalid.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid credentials");
    }
}
