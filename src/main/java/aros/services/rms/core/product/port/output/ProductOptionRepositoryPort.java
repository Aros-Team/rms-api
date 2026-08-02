/* (C) 2026 */

package aros.services.rms.core.product.port.output;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.inventory.domain.ProductRecipe;
import aros.services.rms.core.product.domain.ProductOption;
import aros.services.rms.core.product.domain.ProductOptionCostProfile;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Output port for product option persistence operations. */
public interface ProductOptionRepositoryPort {
  /**
   * Saves a product option to the repository.
   *
   * @param productOption the product option to save
   * @return the saved product option
   */
  ProductOption save(ProductOption productOption);

  /**
   * Finds a product option by its identifier.
   *
   * @param id the product option identifier
   * @return Optional containing the product option if found
   */
  Optional<ProductOption> findById(Long id);

  /**
   * Finds multiple product options by their identifiers.
   *
   * @param ids list of product option identifiers to find
   * @return list of found product options
   */
  List<ProductOption> findAllById(List<Long> ids);

  /**
   * Retrieves all product options.
   *
   * @return list of all product options
   */
  List<ProductOption> findAll();

  /**
   * Finds all product options for a specific product.
   *
   * @param productId the product identifier
   * @return list of product options for the product
   */
  List<ProductOption> findByProductId(Long productId);

  /**
   * Finds all product options whose category matches the given option group.
   *
   * @param optionGroupId the option group identifier
   * @return list of product options attached to the group
   */
  List<ProductOption> findByOptionGroupId(Long optionGroupId);

  /**
   * Loads option pricing and category projection metadata for a product.
   *
   * <p>The persistence projection supplies a SINGLE_CHOICE default when the optional category
   * selection column is null.
   *
   * @param productId the product identifier
   * @return ordered option cost profiles for the product
   */
  List<ProductOptionCostProfile> loadCostProfilesByProductId(Long productId);

  /**
   * Associates multiple options to a product using a no-surcharge, default-order upsert.
   *
   * <p>Convenience wrapper for callers that don't need to set per-option {@code extraPrice} or
   * {@code displayOrder}; for new associations this is equivalent to calling {@link
   * #upsertOptionAssociation} with {@code extraPrice=0} and {@code displayOrder=0}.
   *
   * @param productId the product identifier
   * @param optionIds list of option identifiers to associate
   */
  void associateOptionsToProduct(Long productId, List<Long> optionIds);

  /**
   * Inserts or updates the association of a single option to a product, persisting the
   * per-association {@code extra_price} surcharge (V25+) and {@code display_order} (V25+).
   *
   * <p>The operation is atomic and idempotent: a {@code (productId, optionId)} pair that already
   * exists has its surcharge and display order overwritten.
   *
   * @param productId the product identifier
   * @param optionId the option identifier
   * @param extraPrice the per-association surcharge (nullable → 0)
   * @param displayOrder the display order within the product's option list
   */
  void upsertOptionAssociation(
      Long productId, Long optionId, BigDecimal extraPrice, int displayOrder);

  /**
   * Removes all options from a product.
   *
   * @param productId the product identifier
   */
  void removeAllOptionsFromProduct(Long productId);

  /**
   * Checks if an option is associated with a specific product.
   *
   * @param productId the product identifier
   * @param optionId the option identifier
   * @return true if the option is associated with the product
   */
  boolean isOptionAssociatedWithProduct(Long productId, Long optionId);

  /**
   * Loads the product's base-recipe lines whose supply variant belongs to the given supply
   * category. Used by inventory services to subtract the substitution slot when a SINGLE_CHOICE
   * option is selected from a category that declares a {@code replace_supply_category_id}.
   *
   * <p>For example, if a product's base recipe uses a "Mayonesa" variant (supply category "Salsas")
   * and a SINGLE_CHOICE option category replaces that slot, the returned list contains the {@code
   * (variantId, requiredQuantity)} pair for the Mayonesa line so inventory can subtract it when a
   * non-default option is chosen.
   *
   * @param productId the product identifier
   * @param supplyCategoryId the supply category identifier (the slot being replaced)
   * @return the matching product recipe lines, may be empty if no base recipe uses the slot
   */
  List<ProductRecipe> loadBaseRecipeBySupplyCategory(Long productId, Long supplyCategoryId);

  /**
   * Loads the base-recipe cost of each product's default slot per supply category.
   *
   * @return productId → supplyCategoryId → default-slot cost in COP
   */
  Map<Long, Map<Long, Money>> loadDefaultSlotCostByProductAndCategory();

  /**
   * Bulk loads product options grouped by option-group ID. Used for the {@code GET
   * /products/{id}/option-groups} endpoint.
   *
   * @param productIds the product identifiers
   * @return map of product ID → option-group ID → list of ProductOption
   */
  Map<Long, Map<Long, List<ProductOption>>> loadOptionsByProductAndGroup(
      Collection<Long> productIds);
}
