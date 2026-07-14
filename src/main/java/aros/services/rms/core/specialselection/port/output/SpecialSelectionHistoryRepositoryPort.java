package aros.services.rms.core.specialselection.port.output;

import aros.services.rms.core.specialselection.domain.SpecialSelectionHistory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Output port for special selection change history persistence operations. */
public interface SpecialSelectionHistoryRepositoryPort {
  /**
   * Persists a history entry.
   *
   * @param history the history entry to save
   * @return the saved history entry
   */
  SpecialSelectionHistory save(SpecialSelectionHistory history);

  /**
   * Finds a history entry by its identifier.
   *
   * @param id the history entry identifier
   * @return optional history entry if found
   */
  Optional<SpecialSelectionHistory> findById(Long id);

  /**
   * Finds a history entry by product identifier and version.
   *
   * @param productId the product identifier
   * @param version the version number
   * @return optional history entry if found
   */
  Optional<SpecialSelectionHistory> findByProductIdAndVersion(Long productId, int version);

  /**
   * Finds the current history entry for a product.
   *
   * @param productId the product identifier
   * @return optional current history entry if found
   */
  Optional<SpecialSelectionHistory> findCurrentByProductId(Long productId);

  /**
   * Finds all history entries for a product with pagination.
   *
   * @param productId the product identifier
   * @param pageable pagination parameters
   * @return page of history entries
   */
  Page<SpecialSelectionHistory> findByProductId(Long productId, Pageable pageable);

  /**
   * Finds all history entries for a product within a time range.
   *
   * @param productId the product identifier
   * @param from start of the time range
   * @param to end of the time range
   * @return list of history entries in the range
   */
  List<SpecialSelectionHistory> findByProductIdAndChangedAtBetween(
      Long productId, LocalDateTime from, LocalDateTime to);

  /**
   * Finds the maximum version number recorded for a product.
   *
   * @param productId the product identifier
   * @return the maximum version number, or 0 if none exists
   */
  int findMaxVersionByProductId(Long productId);

  /**
   * Marks all history entries for a product as not current.
   *
   * @param productId the product identifier
   */
  void markAllAsNotCurrent(Long productId);
}
