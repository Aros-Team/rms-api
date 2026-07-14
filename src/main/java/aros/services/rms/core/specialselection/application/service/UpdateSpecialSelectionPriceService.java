package aros.services.rms.core.specialselection.application.service;

import aros.services.rms.core.specialselection.application.exception.SpecialSelectionNotFoundException;
import aros.services.rms.core.specialselection.domain.ChangeType;
import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;
import aros.services.rms.core.specialselection.domain.SpecialSelectionHistory;
import aros.services.rms.core.specialselection.domain.SpecialSelectionSnapshot;
import aros.services.rms.core.specialselection.port.input.UpdateSpecialSelectionPriceUseCase;
import aros.services.rms.core.specialselection.port.output.SpecialSelectionHistoryRepositoryPort;
import aros.services.rms.core.specialselection.port.output.SpecialSelectionRepositoryPort;
import java.time.LocalDateTime;

/**
 * Implementation of the use case to update the base price of a special selection and record the
 * change in the history.
 */
public class UpdateSpecialSelectionPriceService implements UpdateSpecialSelectionPriceUseCase {

  private final SpecialSelectionRepositoryPort repositoryPort;
  private final SpecialSelectionHistoryRepositoryPort historyRepositoryPort;
  private final SpecialSelectionSnapshotService snapshotService;

  /**
   * Creates a new update special selection price service.
   *
   * @param repositoryPort the special selection repository port
   * @param historyRepositoryPort the special selection history repository port
   * @param snapshotService the special selection snapshot service
   */
  public UpdateSpecialSelectionPriceService(
      SpecialSelectionRepositoryPort repositoryPort,
      SpecialSelectionHistoryRepositoryPort historyRepositoryPort,
      SpecialSelectionSnapshotService snapshotService) {
    this.repositoryPort = repositoryPort;
    this.historyRepositoryPort = historyRepositoryPort;
    this.snapshotService = snapshotService;
  }

  @Override
  public SpecialSelectionConfiguration execute(Long productId, Double basePrice, String changedBy) {
    SpecialSelectionConfiguration config =
        repositoryPort
            .findById(productId)
            .orElseThrow(() -> new SpecialSelectionNotFoundException(productId));

    config.setBasePrice(basePrice);
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
            .changeType(ChangeType.PRICE_CHANGE)
            .snapshotJson(snapshotJson)
            .changedBy(changedBy)
            .changedAt(LocalDateTime.now())
            .isCurrent(true)
            .build();

    historyRepositoryPort.save(history);

    return saved;
  }
}
