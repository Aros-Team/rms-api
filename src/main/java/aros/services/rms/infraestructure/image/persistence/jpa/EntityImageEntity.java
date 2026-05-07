/* (C) 2026 */

package aros.services.rms.infraestructure.image.persistence.jpa;

import aros.services.rms.core.image.domain.ImageEntityType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** JPA entity representing an image belonging to any entity type. */
@Entity
@Table(name = "entity_images")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityImageEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "entity_type", nullable = false)
  private ImageEntityType entityType;

  @Column(name = "entity_id", nullable = false)
  private Long entityId;

  @Column(name = "original_filename", nullable = false)
  private String originalFilename;

  @Column(name = "content_type", nullable = false)
  private String contentType;

  @Column(name = "original_size_bytes", nullable = false)
  private Long originalSizeBytes;

  @Column(name = "storage_key", nullable = false)
  private String storageKey;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
}
