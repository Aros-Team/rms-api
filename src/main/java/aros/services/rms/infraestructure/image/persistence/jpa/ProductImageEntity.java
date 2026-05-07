/* (C) 2026 */

package aros.services.rms.infraestructure.image.persistence.jpa;

import aros.services.rms.infraestructure.product.persistence.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** JPA entity representing a product image in the database. */
@Entity
@Table(name = "product_images")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "product_id")
  private Product product;

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
