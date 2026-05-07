/* (C) 2026 */

package aros.services.rms.core.image.port.input;

/** Input port for deleting product images. */
public interface DeleteProductImageUseCase {

  /**
   * Deletes a product image from storage and database.
   *
   * @param imageId the image identifier
   */
  void delete(Long imageId);
}
