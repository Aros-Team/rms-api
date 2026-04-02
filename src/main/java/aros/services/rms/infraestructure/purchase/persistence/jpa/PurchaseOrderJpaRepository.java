/* (C) 2026 */
package aros.services.rms.infraestructure.purchase.persistence.jpa;

import aros.services.rms.infraestructure.purchase.persistence.PurchaseOrderEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data JPA repository for PurchaseOrderEntity. */
public interface PurchaseOrderJpaRepository extends JpaRepository<PurchaseOrderEntity, Long> {

  List<PurchaseOrderEntity> findBySupplierId(Long supplierId);

  List<PurchaseOrderEntity> findByPurchasedAtBetween(LocalDateTime from, LocalDateTime to);
}
