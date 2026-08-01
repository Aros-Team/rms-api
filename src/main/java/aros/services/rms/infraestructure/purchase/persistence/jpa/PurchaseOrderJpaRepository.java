/* (C) 2026 */

package aros.services.rms.infraestructure.purchase.persistence.jpa;

import aros.services.rms.infraestructure.purchase.persistence.PurchaseOrderEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Spring Data JPA repository for PurchaseOrderEntity. */
public interface PurchaseOrderJpaRepository extends JpaRepository<PurchaseOrderEntity, Long> {

  /**
   * Finds purchase orders by notes or supplier name, ignoring case.
   *
   * @param search partial notes or supplier name
   * @return matching purchase order entities
   */
  @Query(
      "SELECT po FROM PurchaseOrderEntity po JOIN po.supplier s "
          + "WHERE LOWER(po.notes) LIKE LOWER(CONCAT('%', :search, '%')) "
          + "OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))")
  List<PurchaseOrderEntity> findByNotesOrSupplierNameContainingIgnoreCase(
      @Param("search") String search);

  /**
   * Finds purchase orders by supplier ID.
   *
   * @param supplierId the supplier ID
   * @return the list of purchase order entities
   */
  List<PurchaseOrderEntity> findBySupplierId(Long supplierId);

  /**
   * Finds purchase orders by date range.
   *
   * @param from the start date
   * @param to the end date
   * @return the list of purchase order entities
   */
  List<PurchaseOrderEntity> findByPurchasedAtBetween(LocalDateTime from, LocalDateTime to);
}
