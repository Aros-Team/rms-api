package aros.services.rms.core.specialselection.application.service;

import aros.services.rms.core.specialselection.application.exception.SpecialSelectionNotFoundException;
import aros.services.rms.core.specialselection.domain.ChangeType;
import aros.services.rms.core.specialselection.domain.SelectionType;
import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;
import aros.services.rms.core.specialselection.domain.SpecialSelectionHistory;
import aros.services.rms.core.specialselection.domain.SpecialSelectionSnapshot;
import aros.services.rms.core.specialselection.port.input.UpdateSpecialSelectionUseCase;
import aros.services.rms.core.specialselection.port.output.SpecialSelectionHistoryRepositoryPort;
import aros.services.rms.core.specialselection.port.output.SpecialSelectionRepositoryPort;
import java.time.LocalDateTime;

/**
 * Implementation of the use case to update an existing special selection configuration and record
 * the change in the history.
 */
public class UpdateSpecialSelectionService implements UpdateSpecialSelectionUseCase {

  private final SpecialSelectionRepositoryPort repositoryPort;
  private final SpecialSelectionHistoryRepositoryPort historyRepositoryPort;
  private final SpecialSelectionSnapshotService snapshotService;
  private final SpecialSelectionValidator validator;

  /**
   * Creates a new update special selection service.
   *
   * @param repositoryPort the special selection repository port
   * @param historyRepositoryPort the special selection history repository port
   * @param snapshotService the special selection snapshot service
   * @param validator the special selection validator
   */
  public UpdateSpecialSelectionService(
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
      Long productId, SpecialSelectionConfiguration config, String changedBy) {
    if (!repositoryPort.existsById(productId)) {
      throw new SpecialSelectionNotFoundException(productId);
    }

    config.setProductId(productId);
    config.setSelectionType(SelectionType.SPECIAL_SELECTION);

    validator.validateConfiguration(config);

    SpecialSelectionConfiguration saved = repositoryPort.save(config);

    int maxVersion = historyRepositoryPort.findMaxVersionByProductId(productId);
    int newVersion = Math.max(maxVersion, 0) + 1;

    historyRepositoryPort.markAllAsNotCurrent(productId);

    SpecialSelectionSnapshot snapshot = snapshotService.fromConfiguration(saved);
    String snapshotJson = snapshotService.toJson(snapshot);

    SpecialSelectionHistory history =
        SpecialSelectionHistory.builder()
            .productId(productId)
            .version(newVersion)
            .changeType(ChangeType.UPDATE)
            .snapshotJson(snapshotJson)
            .changedBy(changedBy)
            .changedAt(LocalDateTime.now())
            .isCurrent(true)
            .build();

    historyRepositoryPort.save(history);

    return saved;
  }
}
