package aros.services.rms.core.specialselection.application.service;

import aros.services.rms.core.specialselection.domain.ChangeType;
import aros.services.rms.core.specialselection.domain.SelectionType;
import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;
import aros.services.rms.core.specialselection.domain.SpecialSelectionHistory;
import aros.services.rms.core.specialselection.domain.SpecialSelectionSnapshot;
import aros.services.rms.core.specialselection.port.input.CreateSpecialSelectionUseCase;
import aros.services.rms.core.specialselection.port.output.SpecialSelectionHistoryRepositoryPort;
import aros.services.rms.core.specialselection.port.output.SpecialSelectionRepositoryPort;
import java.time.LocalDateTime;

/**
 * Implementation of the use case to create a new special selection configuration and persist the
 * initial history entry as the current version.
 */
public class CreateSpecialSelectionService implements CreateSpecialSelectionUseCase {

  private final SpecialSelectionRepositoryPort repositoryPort;
  private final SpecialSelectionHistoryRepositoryPort historyRepositoryPort;
  private final SpecialSelectionSnapshotService snapshotService;
  private final SpecialSelectionValidator validator;

  /**
   * Creates a new create special selection service.
   *
   * @param repositoryPort the special selection repository port
   * @param historyRepositoryPort the special selection history repository port
   * @param snapshotService the special selection snapshot service
   * @param validator the special selection validator
   */
  public CreateSpecialSelectionService(
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
      SpecialSelectionConfiguration config, String createdBy) {
    config.setSelectionType(SelectionType.SPECIAL_SELECTION);
    config.setActive(true);

    validator.validateConfiguration(config);

    SpecialSelectionConfiguration saved = repositoryPort.save(config);

    SpecialSelectionSnapshot snapshot = snapshotService.fromConfiguration(saved);
    String snapshotJson = snapshotService.toJson(snapshot);

    SpecialSelectionHistory history =
        SpecialSelectionHistory.builder()
            .productId(saved.getProductId())
            .version(1)
            .changeType(ChangeType.CREATE)
            .snapshotJson(snapshotJson)
            .changedBy(createdBy)
            .changedAt(LocalDateTime.now())
            .isCurrent(true)
            .build();

    historyRepositoryPort.markAllAsNotCurrent(saved.getProductId());
    historyRepositoryPort.save(history);

    return saved;
  }
}
