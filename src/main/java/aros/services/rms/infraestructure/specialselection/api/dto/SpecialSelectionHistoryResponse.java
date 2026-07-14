package aros.services.rms.infraestructure.specialselection.api.dto;

import aros.services.rms.core.specialselection.domain.SpecialSelectionHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/** DTO representing a special selection change history entry returned by the API. */
@Schema(description = "Special selection history entry")
public record SpecialSelectionHistoryResponse(
    @Schema(description = "History ID") Long id,
    @Schema(description = "Product ID") Long productId,
    @Schema(description = "Version number") int version,
    @Schema(description = "Change type") String changeType,
    @Schema(description = "JSON snapshot") String snapshotJson,
    @Schema(description = "Who changed it") String changedBy,
    @Schema(description = "When changed") LocalDateTime changedAt,
    @Schema(description = "Is current version") boolean isCurrent) {

  /**
   * Maps a domain history entity to its API DTO representation.
   *
   * @param history the domain history entry
   * @return the API DTO, or null if the input is null
   */
  public static SpecialSelectionHistoryResponse fromDomain(SpecialSelectionHistory history) {
    if (history == null) {
      return null;
    }
    return new SpecialSelectionHistoryResponse(
        history.getId(),
        history.getProductId(),
        history.getVersion(),
        history.getChangeType() != null ? history.getChangeType().name() : null,
        history.getSnapshotJson(),
        history.getChangedBy(),
        history.getChangedAt(),
        history.isCurrent());
  }
}
