package aros.services.rms.core.image.domain;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain entity representing an image belonging to any entity type. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityImage {
  private Long id;
  private ImageEntityType entityType;
  private Long entityId;
  private String originalFilename;
  private String contentType;
  private long originalSizeBytes;
  private String storageKey;
  @Builder.Default private Instant createdAt = Instant.now();
}
