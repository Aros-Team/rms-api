/* (C) 2026 */

package aros.services.rms.infraestructure.image.api.dto;

import aros.services.rms.core.image.domain.ImageEntityType;
import aros.services.rms.core.image.domain.ImageWithUrls;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/** Response DTO for entity image data. */
@Schema(description = "Response DTO for entity image data")
public record ImageResponse(
    @Schema(description = "Image ID", example = "1") Long id,
    @Schema(description = "Entity type", example = "PRODUCT") ImageEntityType entityType,
    @Schema(description = "Entity ID", example = "42") Long entityId,
    @Schema(description = "Original filename", example = "burger.jpg") String originalFilename,
    @Schema(description = "Content type", example = "image/webp") String contentType,
    @Schema(description = "Original file size in bytes", example = "204800") long originalSizeBytes,
    @Schema(description = "Mobile version URL") String mobileUrl,
    @Schema(description = "Tablet version URL") String tabletUrl,
    @Schema(description = "Desktop version URL") String desktopUrl,
    @Schema(description = "Created at") Instant createdAt) {

  /** Creates a response from a domain object. */
  public static ImageResponse fromDomain(ImageWithUrls imageWithUrls) {
    if (imageWithUrls == null) {
      return null;
    }
    var image = imageWithUrls.getImage();
    return new ImageResponse(
        image.getId(),
        image.getEntityType(),
        image.getEntityId(),
        image.getOriginalFilename(),
        image.getContentType(),
        image.getOriginalSizeBytes(),
        imageWithUrls.getMobileUrl(),
        imageWithUrls.getTabletUrl(),
        imageWithUrls.getDesktopUrl(),
        image.getCreatedAt());
  }
}
