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
   * Loads the per-product mandatory flag for every option group associated with the product.
   *
   * @param productId the product identifier
   * @return option-group id -> required flag (groups without an association are absent)
   */
  Map<Long, Boolean> loadRequiredFlagsByProductId(Long productId);

  /**
   * Associates an option group with a product, keeping the existing association when present.
   *
   * <p>Used when an option is associated with a product and its group has not been linked yet (for
   * example, legacy data created before V40).
   *
   * @param productId the product identifier
   * @param optionGroupId the option-group identifier
   * @param required whether the group should be mandatory for this product
   */
  void associateProduct(Long productId, Long optionGroupId, boolean required);

  /**
   * Removes the association between a product and an option group without touching the group or its
   * options.
   *
   * @param productId the product identifier
   * @param optionGroupId the option-group identifier
   */
  void detachProduct(Long productId, Long optionGroupId);

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
