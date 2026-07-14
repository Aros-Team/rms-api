package aros.services.rms.core.specialselection.application.service;

import aros.services.rms.core.specialselection.application.exception.SpecialSelectionNotFoundException;
import aros.services.rms.core.specialselection.domain.ChangeType;
import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;
import aros.services.rms.core.specialselection.domain.SpecialSelectionHistory;
import aros.services.rms.core.specialselection.domain.SpecialSelectionScheduleEntry;
import aros.services.rms.core.specialselection.domain.SpecialSelectionSnapshot;
import aros.services.rms.core.specialselection.port.input.UpdateSpecialSelectionScheduleUseCase;
import aros.services.rms.core.specialselection.port.output.SpecialSelectionHistoryRepositoryPort;
import aros.services.rms.core.specialselection.port.output.SpecialSelectionRepositoryPort;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of the use case to update the availability schedule of a special selection and
 * record the change in the history.
 */
public class UpdateSpecialSelectionScheduleService
    implements UpdateSpecialSelectionScheduleUseCase {

  private final SpecialSelectionRepositoryPort repositoryPort;
  private final SpecialSelectionHistoryRepositoryPort historyRepositoryPort;
  private final SpecialSelectionSnapshotService snapshotService;

  /**
   * Creates a new update special selection schedule service.
   *
   * @param repositoryPort the special selection repository port
   * @param historyRepositoryPort the special selection history repository port
   * @param snapshotService the special selection snapshot service
   */
  public UpdateSpecialSelectionScheduleService(
      SpecialSelectionRepositoryPort repositoryPort,
      SpecialSelectionHistoryRepositoryPort historyRepositoryPort,
      SpecialSelectionSnapshotService snapshotService) {
    this.repositoryPort = repositoryPort;
    this.historyRepositoryPort = historyRepositoryPort;
    this.snapshotService = snapshotService;
  }

  @Override
  public SpecialSelectionConfiguration execute(
      Long productId, List<SpecialSelectionScheduleEntry> schedule, String changedBy) {
    SpecialSelectionConfiguration config =
        repositoryPort
            .findById(productId)
            .orElseThrow(() -> new SpecialSelectionNotFoundException(productId));

    config.setSchedule(schedule);
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
            .changeType(ChangeType.SCHEDULE_CHANGE)
            .snapshotJson(snapshotJson)
            .changedBy(changedBy)
            .changedAt(LocalDateTime.now())
            .isCurrent(true)
            .build();

    historyRepositoryPort.save(history);

    return saved;
  }
}
