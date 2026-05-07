package aros.services.rms.core.image.application.exception;

/** Exception thrown when an image is not found. */
public class ImageNotFoundException extends RuntimeException {
  /** Creates exception with image ID. */
  public ImageNotFoundException(Long imageId) {
    super("Image not found with ID: " + imageId);
  }
}
