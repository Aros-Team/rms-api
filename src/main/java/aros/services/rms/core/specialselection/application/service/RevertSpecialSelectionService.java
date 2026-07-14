package aros.services.rms.core.specialselection.application.service;

import aros.services.rms.core.specialselection.application.exception.InvalidSpecialSelectionHistoryException;
import aros.services.rms.core.specialselection.application.exception.SpecialSelectionNotFoundException;
import aros.services.rms.core.specialselection.domain.ChangeType;
import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;
import aros.services.rms.core.specialselection.domain.SpecialSelectionHistory;
import aros.services.rms.core.specialselection.domain.SpecialSelectionSnapshot;
import aros.services.rms.core.specialselection.port.input.RevertSpecialSelectionUseCase;
import aros.services.rms.core.specialselection.port.output.SpecialSelectionHistoryRepositoryPort;
import aros.services.rms.core.specialselection.port.output.SpecialSelectionRepositoryPort;
import java.time.LocalDateTime;

/**
 * Implementation of the use case to revert a special selection to a previous history version and
 * record the reversion as a new update entry.
 */
public class RevertSpecialSelectionService implements RevertSpecialSelectionUseCase {

  private final SpecialSelectionRepositoryPort repositoryPort;
  private final SpecialSelectionHistoryRepositoryPort historyRepositoryPort;
  private final SpecialSelectionSnapshotService snapshotService;
  private final SpecialSelectionValidator validator;

  /**
   * Creates a new revert special selection service.
   *
   * @param repositoryPort the special selection repository port
   * @param historyRepositoryPort the special selection history repository port
   * @param snapshotService the special selection snapshot service
   * @param validator the special selection validator
   */
  public RevertSpecialSelectionService(
      SpecialSelectionRepositoryPort repositoryPort,
      SpecialSelectionHistoryRepositoryPort historyRepositoryPort,
      SpecialSelectionSnapshotService snapshotService,
      SpecialSelectionValidator validator) {
    this.repositoryPort = repositoryPort;
    this.historyRepositoryPort = historyRepositoryPort;
    this.snapshotService = snapshotService;
    this.validator = validator;
  }

  @Override
  public SpecialSelectionConfiguration execute(
      Long productId, int targetVersion, String changedBy) {
    if (!repositoryPort.existsById(productId)) {
      throw new SpecialSelectionNotFoundException(productId);
    }

    SpecialSelectionHistory targetHistory =
        historyRepositoryPort
            .findByProductIdAndVersion(productId, targetVersion)
            .orElseThrow(
                () ->
                    new InvalidSpecialSelectionHistoryException(
                        "Version " + targetVersion + " not found for product " + productId));

    SpecialSelectionSnapshot snapshot = snapshotService.fromJson(targetHistory.getSnapshotJson());

    SpecialSelectionConfiguration restored = snapshotService.toConfiguration(snapshot);
    restored.setProductId(productId);

    validator.validateConfiguration(restored);

    SpecialSelectionConfiguration saved = repositoryPort.save(restored);

    int maxVersion = historyRepositoryPort.findMaxVersionByProductId(productId);
    int newVersion = Math.max(maxVersion, 0) + 1;

    historyRepositoryPort.markAllAsNotCurrent(productId);

    SpecialSelectionSnapshot newSnapshot = snapshotService.fromConfiguration(saved);
    String snapshotJson = snapshotService.toJson(newSnapshot);

    SpecialSelectionHistory newHistory =
        SpecialSelectionHistory.builder()
            .productId(productId)
            .version(newVersion)
            .changeType(ChangeType.UPDATE)
            .snapshotJson(snapshotJson)
            .changedBy(changedBy)
            .changedAt(LocalDateTime.now())
            .isCurrent(true)
            .build();

    historyRepositoryPort.save(newHistory);

    return saved;
  }
}
