package aros.services.rms.core.image.domain;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain entity representing a product image with storage metadata. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {
  private Long id;
  private Long productId;
  private String originalFilename;
  private String contentType;
  private long originalSizeBytes;
  private String storageKey;
  @Builder.Default private Instant createdAt = Instant.now();
}
