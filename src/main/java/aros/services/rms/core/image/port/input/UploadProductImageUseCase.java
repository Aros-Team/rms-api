/* (C) 2026 */

package aros.services.rms.core.image.port.input;

import aros.services.rms.core.image.domain.ProductImage;

/** Input port for uploading product images. */
public interface UploadProductImageUseCase {
  /**
   * Uploads a product image, processing it into multiple size versions.
   *
   * @param productId the product identifier
   * @param originalFilename the original file name
   * @param contentType the MIME type of the image
   * @param imageData the raw image bytes
   * @return the saved product image
   */
  ProductImage upload(
      Long productId, String originalFilename, String contentType, byte[] imageData);
}
