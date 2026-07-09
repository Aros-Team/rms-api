/* (C) 2026 */

package aros.services.rms.infraestructure.image.api.dto;

import aros.services.rms.core.image.domain.ImageEntityType;
import aros.services.rms.core.image.domain.ImageWithUrls;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Image response DTO")
public record ImageResponse(
    @Schema(description = "Unique image ID", example = "1") Long id,
    @Schema(description = "Entity type", example = "PRODUCT") ImageEntityType entityType,
    @Schema(description = "ID of the owning entity", example = "42") Long entityId,
    @Schema(description = "Original filename", example = "burger.jpg") String originalFilename,
    @Schema(description = "MIME content type", example = "image/webp") String contentType,
    @Schema(description = "Original file size in bytes", example = "204800") long originalSizeBytes,
    @Schema(description = "Mobile-optimized version URL") String mobileUrl,
    @Schema(description = "Tablet-optimized version URL") String tabletUrl,
    @Schema(description = "Desktop-optimized version URL") String desktopUrl,
    @Schema(description = "Creation timestamp") Instant createdAt) {

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
