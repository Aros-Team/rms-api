/* (C) 2026 */

package aros.services.rms.core.image.port.input;

import aros.services.rms.core.image.domain.ProductImageWithUrls;
import java.util.List;

/** Input port for retrieving product images. */
public interface GetProductImagesUseCase {

  /**
   * Retrieves all images for a product, each with signed URLs.
   *
   * @param productId the product identifier
   * @return list of product images with signed URLs
   */
  List<ProductImageWithUrls> getByProductId(Long productId);

  /**
   * Retrieves a single image by its ID with signed URLs.
   *
   * @param imageId the image identifier
   * @return the product image with signed URLs
   */
  ProductImageWithUrls getById(Long imageId);
}
