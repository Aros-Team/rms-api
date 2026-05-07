package aros.services.rms.core.image.application.exception;

/** Thrown when image upload or storage operation fails. */
public class ImageUploadException extends RuntimeException {
  /** Creates exception with message. */
  public ImageUploadException(String message) {
    super(message);
  }
}
