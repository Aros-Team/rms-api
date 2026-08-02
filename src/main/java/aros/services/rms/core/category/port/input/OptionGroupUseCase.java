/* (C) 2026 */

package aros.services.rms.core.category.port.input;

import aros.services.rms.core.category.domain.OptionGroup;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Input port for option group management. Option groups define customization buckets for products
 * (e.g., "Proteína Hamburguesa", "Acompañamiento Parrilla"). They are different from product
 * categories and from Special Selections (which model product groups + advanced choices).
 *
 * <p>An option group must always be associated with at least one product (business rule).
 */
public interface OptionGroupUseCase {

  /**
   * Creates a new option group, attaching it to the supplied product IDs.
   *
   * @param optionGroup the option group data to create
   * @param productIds the IDs of the products this group applies to (must contain at least one)
   * @param required the required flag applied to every product association in this group
   * @return the created option group with generated ID
   * @throws
   *     aros.services.rms.core.category.application.exception.OptionGroupRequiresProductException
   *     if {@code productIds} is empty
   */
  OptionGroup create(OptionGroup optionGroup, List<Long> productIds, boolean required);

  /**
   * Updates an existing option group and replaces its product associations.
   *
   * @param id the option group identifier
   * @param optionGroup the option group data with updates
   * @param productIds the IDs of the products this group applies to (must contain at least one)
   * @param required the required flag applied to every product association in this group
   * @return the updated option group
   * @throws
   *     aros.services.rms.core.category.application.exception.OptionGroupRequiresProductException
   *     if {@code productIds} is empty
   */
  OptionGroup update(Long id, OptionGroup optionGroup, List<Long> productIds, boolean required);

  /**
   * Retrieves all option groups.
   *
   * @return list of all option groups
   */
  List<OptionGroup> findAll();

  /**
   * Finds option groups whose name contains the given string (case-insensitive).
   *
   * @param name the partial name to search for
   * @return list of matching option groups
   */
  List<OptionGroup> findByNameContainingIgnoreCase(String name);

  /**
   * Loads selection types for the requested option groups.
   *
   * @param ids group identifiers
   * @return selection type keyed by group identifier
   */
  Map<Long, String> loadSelectionTypesByIds(Collection<Long> ids);

  /**
   * Finds an option group by its identifier.
   *
   * @param id the option group identifier
   * @return the found option group
   */
  OptionGroup findById(Long id);

  /**
   * Finds option groups associated with the given product.
   *
   * @param productId the product identifier
   * @return list of option groups attached to the product
   */
  List<OptionGroup> findByProductId(Long productId);

  /**
   * Bulk lookup of product IDs grouped by option-group ID. Used for enrichment in list endpoints.
   *
   * @param optionGroupIds the option-group identifiers
   * @return map of option-group ID -> list of associated product IDs
   */
  Map<Long, List<Long>> loadProductIdsByOptionGroupIds(Collection<Long> optionGroupIds);
}
