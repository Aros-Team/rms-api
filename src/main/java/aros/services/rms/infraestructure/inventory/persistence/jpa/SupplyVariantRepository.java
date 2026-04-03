/* (C) 2026 */
package aros.services.rms.infraestructure.inventory.persistence.jpa;

import aros.services.rms.infraestructure.inventory.persistence.SupplyVariantEntity;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplyVariantRepository extends JpaRepository<SupplyVariantEntity, Long> {

  /** Finds all variants belonging to a specific supply. */
  List<SupplyVariantEntity> findBySupplyId(Long supplyId);

  /** Checks uniqueness before insert: same supply + unit + quantity. */
  Optional<SupplyVariantEntity> findBySupplyIdAndUnitIdAndQuantity(
      Long supplyId, Long unitId, BigDecimal quantity);
}
