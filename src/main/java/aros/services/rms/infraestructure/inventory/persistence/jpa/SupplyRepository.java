/* (C) 2026 */
package aros.services.rms.infraestructure.inventory.persistence.jpa;

import aros.services.rms.infraestructure.inventory.persistence.SupplyEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JPA repository for supplies. */
@Repository
public interface SupplyRepository extends JpaRepository<SupplyEntity, Long> {

  /**
   * Finds a supply by name (case-insensitive).
   *
   * @param name the name
   * @return the supply entity if found
   */
  Optional<SupplyEntity> findByNameIgnoreCase(String name);

  /**
   * Finds supplies whose name contains the given string (case-insensitive).
   *
   * @param name the name substring
   * @return the list of supply entities
   */
  List<SupplyEntity> findByNameContainingIgnoreCase(String name);

  /**
   * Finds supplies belonging to a specific category.
   *
   * @param categoryId the category ID
   * @return the list of supply entities
   */
  List<SupplyEntity> findByCategoryId(Long categoryId);

  /**
   * Finds supplies by category and name (combined filter).
   *
   * @param categoryId the category ID
   * @param name the name substring
   * @return the list of supply entities
   */
  List<SupplyEntity> findByCategoryIdAndNameContainingIgnoreCase(Long categoryId, String name);
}
