package aros.services.rms.core.image.application.exception;

/** Thrown for invalid image format, size, or content. */
public class InvalidImageException extends RuntimeException {
  /** Creates exception with message. */
  public InvalidImageException(String message) {
    super(message);
  }
}
