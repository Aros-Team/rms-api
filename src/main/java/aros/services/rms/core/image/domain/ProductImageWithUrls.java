package aros.services.rms.core.image.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Product image with generated signed URLs for each size. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageWithUrls {
  private ProductImage image;
  private String mobileUrl;
  private String tabletUrl;
  private String desktopUrl;
}
