/* (C) 2026 */

package aros.services.rms.infraestructure.product.persistence.jpa;

import aros.services.rms.infraestructure.product.persistence.ProductOption;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for ProductOption entities. */
@Repository
public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {

  /** Finds all options for a given product. */
  @Query(
      value =
          "SELECT po.* FROM product_options po "
              + "JOIN product_product_options ppo ON po.id = ppo.option_id "
              + "WHERE ppo.product_id = :productId",
      nativeQuery = true)
  List<ProductOption> findByProductId(@Param("productId") Long productId);

  /** Finds all options attached to a given option group (via {@code option_category_id}). */
  @Query(
      value = "SELECT * FROM product_options WHERE option_category_id = :optionGroupId",
      nativeQuery = true)
  List<ProductOption> findByOptionGroupId(@Param("optionGroupId") Long optionGroupId);

  /** Removes all options from a product. */
  @Modifying
  @Query(
      value = "DELETE FROM product_product_options WHERE product_id = :productId",
      nativeQuery = true)
  void removeAllOptionsFromProduct(@Param("productId") Long productId);

  /**
   * Associates an option to a product. Used by the simple no-surcharge code path; new rows get the
   * V25 default of {@code extra_price = 0} and {@code display_order = 0}.
   */
  @Modifying
  @Query(
      value =
          "INSERT INTO product_product_options (product_id, option_id) "
              + "VALUES (:productId, :optionId)",
      nativeQuery = true)
  void associateOptionToProduct(
      @Param("productId") Long productId, @Param("optionId") Long optionId);

  /**
   * Inserts or updates the (product_id, option_id) association, persisting the V25 columns {@code
   * extra_price} and {@code display_order}. Idempotent — re-running the same call updates the
   * surcharge and ordering.
   */
  @Modifying
  @Query(
      value =
          "INSERT INTO product_product_options "
              + "  (product_id, option_id, extra_price, display_order) "
              + "VALUES (:productId, :optionId, :extraPrice, :displayOrder) "
              + "ON DUPLICATE KEY UPDATE "
              + "  extra_price   = VALUES(extra_price), "
              + "  display_order = VALUES(display_order)",
      nativeQuery = true)
  void upsertOptionAssociation(
      @Param("productId") Long productId,
      @Param("optionId") Long optionId,
      @Param("extraPrice") BigDecimal extraPrice,
      @Param("displayOrder") int displayOrder);

  /** Removes one option association from a product. */
  @Modifying
  @Query(
      value =
          "DELETE FROM product_product_options "
              + "WHERE product_id = :productId AND option_id = :optionId",
      nativeQuery = true)
  void removeOptionFromProduct(
      @Param("productId") Long productId, @Param("optionId") Long optionId);

  /** Removes every product option that belongs to the given option group. */
  @Modifying
  @Query(
      value =
          "DELETE ppo FROM product_product_options ppo "
              + "JOIN product_options po ON po.id = ppo.option_id "
              + "WHERE ppo.product_id = :productId "
              + "AND po.option_category_id = :optionGroupId",
      nativeQuery = true)
  void removeOptionsByGroupFromProduct(
      @Param("productId") Long productId, @Param("optionGroupId") Long optionGroupId);

  /** Updates the per-product surcharge of an existing option association. */
  @Modifying
  @Query(
      value =
          "UPDATE product_product_options SET extra_price = :extraPrice "
              + "WHERE product_id = :productId AND option_id = :optionId",
      nativeQuery = true)
  void updateOptionExtraPrice(
      @Param("productId") Long productId,
      @Param("optionId") Long optionId,
      @Param("extraPrice") BigDecimal extraPrice);

  /** Returns the next display order for a new option association. */
  @Query(
      value =
          "SELECT COALESCE(MAX(display_order), -1) + 1 FROM product_product_options "
              + "WHERE product_id = :productId",
      nativeQuery = true)
  Integer nextDisplayOrder(@Param("productId") Long productId);

  /**
   * Loads the option groups marked as required for a product.
   *
   * @param productId the product identifier
   * @return rows of group id + name
   */
  @Query(
      value =
          "SELECT og.id AS optionGroupId, og.name AS optionGroupName "
              + "FROM product_option_groups pog "
              + "JOIN option_group og ON og.id = pog.option_group_id "
              + "WHERE pog.product_id = :productId AND pog.required = TRUE",
      nativeQuery = true)
  List<RequiredGroupProjection> findRequiredOptionGroupsByProductId(
      @Param("productId") Long productId);

  /** Checks if an option is associated with a product. */
  @Query(
      value =
          "SELECT COUNT(*) FROM product_product_options "
              + "WHERE product_id = :productId AND option_id = :optionId",
      nativeQuery = true)
  Long isOptionAssociatedWithProduct(
      @Param("productId") Long productId, @Param("optionId") Long optionId);

  /** Projection for a required option group of a product. */
  interface RequiredGroupProjection {
    Long getOptionGroupId();

    String getOptionGroupName();
  }
}
