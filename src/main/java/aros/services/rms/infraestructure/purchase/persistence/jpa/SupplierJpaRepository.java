/* (C) 2026 */

package aros.services.rms.infraestructure.purchase.persistence.jpa;

import aros.services.rms.infraestructure.purchase.persistence.SupplierEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data JPA repository for SupplierEntity. */
public interface SupplierJpaRepository extends JpaRepository<SupplierEntity, Long> {

  /**
   * Finds suppliers whose names contain the supplied text, ignoring case.
   *
   * @param name partial supplier name
   * @return matching supplier entities
   */
  List<SupplierEntity> findByNameContainingIgnoreCase(String name);
}
