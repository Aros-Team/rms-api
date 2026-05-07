/* (C) 2026 */

package aros.services.rms.infraestructure.image.api.dto;

import aros.services.rms.core.image.domain.ProductImageWithUrls;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/** Response DTO for product image data. */
@Schema(description = "Response DTO for product image data")
public record ImageResponse(
    @Schema(description = "Image ID", example = "1") Long id,
    @Schema(description = "Product ID", example = "42") Long productId,
    @Schema(description = "Original filename", example = "burger.jpg") String originalFilename,
    @Schema(description = "Content type", example = "image/webp") String contentType,
    @Schema(description = "Original file size in bytes", example = "204800") long originalSizeBytes,
    @Schema(description = "Mobile version URL") String mobileUrl,
    @Schema(description = "Tablet version URL") String tabletUrl,
    @Schema(description = "Desktop version URL") String desktopUrl,
    @Schema(description = "Created at") Instant createdAt) {

  /**
   * Creates a response from a domain object.
   *
   * @param imageWithUrls the product image with signed URLs
   * @return the response DTO
   */
  public static ImageResponse fromDomain(ProductImageWithUrls imageWithUrls) {
    if (imageWithUrls == null) {
      return null;
    }
    return new ImageResponse(
        imageWithUrls.getImage().getId(),
        imageWithUrls.getImage().getProductId(),
        imageWithUrls.getImage().getOriginalFilename(),
        imageWithUrls.getImage().getContentType(),
        imageWithUrls.getImage().getOriginalSizeBytes(),
        imageWithUrls.getMobileUrl(),
        imageWithUrls.getTabletUrl(),
        imageWithUrls.getDesktopUrl(),
        imageWithUrls.getImage().getCreatedAt());
  }
}
