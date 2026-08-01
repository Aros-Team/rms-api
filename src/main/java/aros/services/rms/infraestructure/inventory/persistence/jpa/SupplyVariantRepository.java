/* (C) 2026 */

package aros.services.rms.infraestructure.inventory.persistence.jpa;

import aros.services.rms.infraestructure.inventory.persistence.SupplyVariantEntity;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** JPA repository for supply variants. */
@Repository
public interface SupplyVariantRepository extends JpaRepository<SupplyVariantEntity, Long> {

  /**
   * Finds all variants belonging to a specific supply.
   *
   * @param supplyId the supply ID
   * @return the list of supply variant entities
   */
  List<SupplyVariantEntity> findBySupplyId(Long supplyId);

  /**
   * Finds all variants belonging to a specific supply, paginated.
   *
   * @param supplyId the supply ID
   * @param pageable the pagination information
   * @return the page of supply variant entities
   */
  Page<SupplyVariantEntity> findBySupplyId(Long supplyId, Pageable pageable);

  /**
   * Finds all variants belonging to a specific supply and matching a name filter
   * (case-insensitive), paginated.
   *
   * @param supplyId the supply ID
   * @param name the name substring
   * @param pageable the pagination information
   * @return the page of supply variant entities
   */
  @Query(
      "SELECT sv FROM SupplyVariantEntity sv JOIN sv.supply s "
          + "WHERE sv.supply.id = :supplyId AND LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))")
  Page<SupplyVariantEntity> findBySupplyIdAndNameContainingIgnoreCase(
      @Param("supplyId") Long supplyId, @Param("name") String name, Pageable pageable);

  /**
   * Checks uniqueness before insert: same supply + unit + quantity.
   *
   * @param supplyId the supply ID
   * @param unitId the unit ID
   * @param quantity the quantity
   * @return the supply variant entity if found
   */
  Optional<SupplyVariantEntity> findBySupplyIdAndUnitIdAndQuantity(
      Long supplyId, Long unitId, BigDecimal quantity);

  /**
   * Finds variants whose supply name contains the given string (case-insensitive), paginated.
   *
   * @param name the name substring
   * @param pageable the pagination information
   * @return the page of supply variant entities
   */
  @Query(
      "SELECT sv FROM SupplyVariantEntity sv JOIN sv.supply s "
          + "WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))")
  Page<SupplyVariantEntity> findByNameContainingIgnoreCase(
      @Param("name") String name, Pageable pageable);
}
