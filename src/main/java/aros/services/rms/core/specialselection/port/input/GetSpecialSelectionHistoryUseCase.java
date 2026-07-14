package aros.services.rms.core.specialselection.port.input;

import aros.services.rms.core.specialselection.domain.SpecialSelectionHistory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Input port for querying the special selection change history. */
public interface GetSpecialSelectionHistoryUseCase {
  /**
   * Retrieves the change history for a product with pagination.
   *
   * @param productId the product identifier
   * @param pageable pagination parameters
   * @return page of history entries
   */
  Page<SpecialSelectionHistory> getHistory(Long productId, Pageable pageable);

  /**
   * Retrieves a single history entry by product and version.
   *
   * @param productId the product identifier
   * @param version the history version
   * @return optional history entry if found
   */
  Optional<SpecialSelectionHistory> getVersion(Long productId, int version);

  /**
   * Retrieves history entries within a given time range.
   *
   * @param productId the product identifier
   * @param from start of the time range (inclusive)
   * @param to end of the time range (inclusive)
   * @return list of history entries in the range
   */
  List<SpecialSelectionHistory> getHistoryBetween(
      Long productId, LocalDateTime from, LocalDateTime to);
}
