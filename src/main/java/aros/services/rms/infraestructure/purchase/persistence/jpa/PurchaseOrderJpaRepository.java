/* (C) 2026 */
package aros.services.rms.infraestructure.purchase.persistence.jpa;

import aros.services.rms.infraestructure.purchase.persistence.PurchaseOrderEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data JPA repository for PurchaseOrderEntity. */
public interface PurchaseOrderJpaRepository extends JpaRepository<PurchaseOrderEntity, Long> {

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
