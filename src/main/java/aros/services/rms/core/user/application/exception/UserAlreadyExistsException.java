package aros.services.rms.core.user.application.exception;

public class UserAlreadyExistsException extends Exception {
    public UserAlreadyExistsException() {
        super();
    }

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
