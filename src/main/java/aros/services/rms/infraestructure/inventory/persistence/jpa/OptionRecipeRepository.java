/* (C) 2026 */

package aros.services.rms.infraestructure.inventory.persistence.jpa;

import aros.services.rms.infraestructure.inventory.persistence.OptionRecipeEntity;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** JPA repository for option recipes. */
@Repository
public interface OptionRecipeRepository extends JpaRepository<OptionRecipeEntity, Long> {

  /**
   * Finds option recipes by option IDs.
   *
   * @param optionIds the list of option IDs
   * @return the list of option recipe entities
   */
  List<OptionRecipeEntity> findByOptionIdIn(List<Long> optionIds);

  /** Aggregates material cost for all requested options in one native query. */
  @Query(
      value =
          "SELECT ore.option_id AS optionId, "
              + "SUM(ore.required_quantity * sv.unit_cost) AS cost "
              + "FROM option_recipes ore "
              + "JOIN supply_variants sv ON sv.id = ore.supply_variant_id "
              + "WHERE ore.option_id IN (:optionIds) "
              + "GROUP BY ore.option_id",
      nativeQuery = true)
  List<OptionMaterialCostProjection> loadMaterialCostByOptionIds(
      @Param("optionIds") Collection<Long> optionIds);

  /** Native material-cost aggregation row. */
  interface OptionMaterialCostProjection {

    /** Returns the option identifier. */
    Long getOptionId();

    /** Returns the aggregated material cost. */
    BigDecimal getCost();
  }
}
