package aros.services.rms.infraestructure.specialselection.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** JPA entity representing a single change entry in the special selection change history. */
@Entity
@Table(name = "special_selection_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialSelectionHistoryEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "product_id", nullable = false)
  private Long productId;

  @Column(nullable = false)
  private int version;

  @Column(name = "change_type", nullable = false, length = 20)
  private String changeType;

  @Column(name = "snapshot_json", nullable = false, columnDefinition = "LONGTEXT")
  private String snapshotJson;

  @Column(name = "changed_by", nullable = false)
  private String changedBy;

  @Column(name = "changed_at", nullable = false)
  private LocalDateTime changedAt;

  @Column(name = "is_current", nullable = false)
  @Builder.Default
  private boolean isCurrent = false;
}
