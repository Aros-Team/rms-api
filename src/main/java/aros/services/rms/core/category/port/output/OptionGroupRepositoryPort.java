/* (C) 2026 */

package aros.services.rms.core.category.port.output;

import aros.services.rms.core.category.domain.OptionGroup;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Output port for option group persistence operations. */
public interface OptionGroupRepositoryPort {
  /**
   * Saves an option group to the repository.
   *
   * @param optionGroup the option group to save
   * @return the saved option group with generated ID
   */
  OptionGroup save(OptionGroup optionGroup);

  /**
   * Finds an option group by its identifier.
   *
   * @param id the option group identifier
   * @return Optional containing the option group if found
   */
  Optional<OptionGroup> findById(Long id);

  /**
   * Finds option groups whose name contains the given string (case-insensitive).
   *
   * @param name the partial name to search for
   * @return list of matching option groups
   */
  List<OptionGroup> findByNameContainingIgnoreCase(String name);

  /**
   * Retrieves all option groups.
   *
   * @return list of all option groups
   */
  List<OptionGroup> findAll();

  /**
   * Loads selection type projections for option groups.
   *
   * @param ids group identifiers
   * @return selection type keyed by group identifier
   */
  Map<Long, String> loadSelectionTypesByIds(Collection<Long> ids);

  /**
   * Checks if an option group exists by its identifier.
   *
   * @param id the option group identifier to check
   * @return true if the option group exists
   */
  boolean existsById(Long id);

  /**
   * Finds option groups associated with the given product via the {@code product_option_groups}
   * junction table.
   *
   * @param productId the product identifier
   * @return list of option groups the product is associated with
   */
  List<OptionGroup> findByProductId(Long productId);

  /**
   * Bulk lookup of product IDs grouped by option-group ID. Avoids N+1 in service-layer enrichment.
   *
   * @param optionGroupIds the option-group identifiers
   * @return map of option-group ID -> list of associated product IDs
   */
  Map<Long, List<Long>> loadProductIdsByOptionGroupIds(Collection<Long> optionGroupIds);

  /**
   * Atomically replaces all product associations for the given option group with the supplied IDs.
   * Each association row is created with the supplied {@code required} flag (same value applied to
   * every association in this group).
   *
   * @param optionGroupId the option group identifier
   * @param productIds the new set of product IDs (may be empty)
   * @param required the required flag applied to every association in this group
   */
  void replaceProductAssociations(Long optionGroupId, List<Long> productIds, boolean required);
}
