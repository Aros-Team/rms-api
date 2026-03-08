package aros.services.rms.core.auth.application.exception;

public class InvalidRefreshToken extends Exception {
    public InvalidRefreshToken() {
        super();
    }

    public InvalidRefreshToken(String message) {
        super(message);
    }
}
