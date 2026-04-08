/* (C) 2026 */
package aros.services.rms.infraestructure.inventory.persistence.jpa;

import aros.services.rms.infraestructure.inventory.persistence.SupplyEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplyRepository extends JpaRepository<SupplyEntity, Long> {

  Optional<SupplyEntity> findByNameIgnoreCase(String name);

  /** Finds supplies whose name contains the given string (case-insensitive). */
  List<SupplyEntity> findByNameContainingIgnoreCase(String name);

  /** Finds supplies belonging to a specific category. */
  List<SupplyEntity> findByCategoryId(Long categoryId);

  /** Finds supplies by category and name (combined filter). */
  List<SupplyEntity> findByCategoryIdAndNameContainingIgnoreCase(Long categoryId, String name);
}
