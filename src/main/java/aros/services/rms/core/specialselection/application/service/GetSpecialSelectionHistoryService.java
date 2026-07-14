package aros.services.rms.core.specialselection.application.service;

import aros.services.rms.core.specialselection.domain.SpecialSelectionHistory;
import aros.services.rms.core.specialselection.port.input.GetSpecialSelectionHistoryUseCase;
import aros.services.rms.core.specialselection.port.output.SpecialSelectionHistoryRepositoryPort;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Implementation of the use case to query special selection change history, including paginated
 * lookups, specific version retrieval and time-range queries.
 */
public class GetSpecialSelectionHistoryService implements GetSpecialSelectionHistoryUseCase {

  private final SpecialSelectionHistoryRepositoryPort historyRepositoryPort;

  /**
   * Creates a new get special selection history service.
   *
   * @param historyRepositoryPort the special selection history repository port
   */
  public GetSpecialSelectionHistoryService(
      SpecialSelectionHistoryRepositoryPort historyRepositoryPort) {
    this.historyRepositoryPort = historyRepositoryPort;
  }

  @Override
  public Page<SpecialSelectionHistory> getHistory(Long productId, Pageable pageable) {
    return historyRepositoryPort.findByProductId(productId, pageable);
  }

  @Override
  public Optional<SpecialSelectionHistory> getVersion(Long productId, int version) {
    return historyRepositoryPort.findByProductIdAndVersion(productId, version);
  }

  @Override
  public List<SpecialSelectionHistory> getHistoryBetween(
      Long productId, LocalDateTime from, LocalDateTime to) {
    return historyRepositoryPort.findByProductIdAndChangedAtBetween(productId, from, to);
  }
}
