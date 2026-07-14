package aros.services.rms.core.specialselection.domain;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single change entry in the history of a special selection, including the snapshot of
 * the configuration at that point in time.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialSelectionHistory {
  private Long id;
  private Long productId;
  private int version;
  private ChangeType changeType;
  private String snapshotJson;
  private String changedBy;
  private LocalDateTime changedAt;
  private boolean isCurrent;
}
