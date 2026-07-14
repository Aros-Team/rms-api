package aros.services.rms.infraestructure.specialselection.persistence.jpa;

import aros.services.rms.infraestructure.specialselection.persistence.SpecialSelectionHistoryEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Spring Data JPA repository for special selection change history entries. */
@Repository
public interface SpecialSelectionHistoryRepository
    extends JpaRepository<SpecialSelectionHistoryEntity, Long> {
  /**
   * Finds a history entry by product identifier and version.
   *
   * @param productId the product identifier
   * @param version the version number
   * @return optional history entry if found
   */
  Optional<SpecialSelectionHistoryEntity> findByProductIdAndVersion(Long productId, int version);

  /**
   * Finds the most recent current history entry for the given product.
   *
   * @param productId the product identifier
   * @param isCurrent the current flag
   * @return optional history entry if found
   */
  Optional<SpecialSelectionHistoryEntity> findFirstByProductIdAndIsCurrentOrderByVersionDesc(
      Long productId, boolean isCurrent);

  /**
   * Finds the history entries for a product with pagination.
   *
   * @param productId the product identifier
   * @param pageable pagination parameters
   * @return page of history entries
   */
  Page<SpecialSelectionHistoryEntity> findByProductId(Long productId, Pageable pageable);

  /**
   * Finds the history entries for a product within a time range.
   *
   * @param productId the product identifier
   * @param from start of the time range
   * @param to end of the time range
   * @return list of history entries
   */
  List<SpecialSelectionHistoryEntity> findByProductIdAndChangedAtBetween(
      Long productId, java.time.LocalDateTime from, java.time.LocalDateTime to);

  /**
   * Finds the maximum version recorded for a product.
   *
   * @param productId the product identifier
   * @return the maximum version, or 0 if none exists
   */
  @Query(
      "SELECT COALESCE(MAX(h.version), 0) FROM SpecialSelectionHistoryEntity h"
          + " WHERE h.productId = :productId")
  int findMaxVersionByProductId(@Param("productId") Long productId);

  /**
   * Marks every current history entry for a product as not current.
   *
   * @param productId the product identifier
   */
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      "UPDATE SpecialSelectionHistoryEntity h SET h.isCurrent = false"
          + " WHERE h.productId = :productId AND h.isCurrent = true")
  void markAllAsNotCurrent(@Param("productId") Long productId);
}
