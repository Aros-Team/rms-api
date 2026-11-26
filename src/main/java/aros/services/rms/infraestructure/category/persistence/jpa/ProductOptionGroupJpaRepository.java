/* (C) 2026 */

package aros.services.rms.infraestructure.category.persistence.jpa;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** JPA repository for the {@code product_option_groups} junction table. */
@Repository
public interface ProductOptionGroupJpaRepository
    extends JpaRepository<ProductOptionGroupEntity, ProductOptionGroupId> {

  /**
   * Finds option-group IDs associated with the given product.
   *
   * @param productId the product identifier
   * @return list of option-group IDs (order not guaranteed)
   */
  @Query(
      "SELECT pog.id.optionGroupId "
          + "FROM ProductOptionGroupEntity pog "
          + "WHERE pog.id.productId = :productId")
  List<Long> findOptionGroupIdsByProductId(@Param("productId") Long productId);

  /**
   * Finds product IDs associated with the given option group.
   *
   * @param optionGroupId the option-group identifier
   * @return list of product IDs
   */
  @Query(
      "SELECT pog.id.productId "
          + "FROM ProductOptionGroupEntity pog "
          + "WHERE pog.id.optionGroupId = :optionGroupId")
  List<Long> findProductIdsByOptionGroupId(@Param("optionGroupId") Long optionGroupId);

  /**
   * Bulk lookup of product IDs grouped by option-group ID. Avoids N+1 in service-layer enrichment.
   *
   * @param optionGroupIds the option-group identifiers
   * @return map of option-group ID -> list of associated product IDs
   */
  @Query(
      "SELECT pog.id.optionGroupId AS optionGroupId, pog.id.productId AS productId "
          + "FROM ProductOptionGroupEntity pog "
          + "WHERE pog.id.optionGroupId IN :optionGroupIds")
  List<ProductIdProjection> findProductIdsByOptionGroupIds(
      @Param("optionGroupIds") List<Long> optionGroupIds);

  /**
   * Loads the per-product mandatory flag for each option group associated with a product.
   *
   * @param productId the product identifier
   * @return projections with option-group id and required flag
   */
  @Query(
      "SELECT pog.id.optionGroupId AS optionGroupId, pog.required AS required "
          + "FROM ProductOptionGroupEntity pog "
          + "WHERE pog.id.productId = :productId")
  List<GroupRequiredProjection> findGroupRequiredByProductId(@Param("productId") Long productId);

  /**
   * Inserts a product/group association if it does not exist yet. Keeps the existing row (and its
   * required flag) untouched when present.
   */
  @Modifying
  @Query(
      value =
          "INSERT IGNORE INTO product_option_groups (product_id, option_group_id, required) "
              + "VALUES (:productId, :optionGroupId, :required)",
      nativeQuery = true)
  void insertIgnoreAssociation(
      @Param("productId") Long productId,
      @Param("optionGroupId") Long optionGroupId,
      @Param("required") boolean required);

  /** Removes one product association for the given option group. */
  @Modifying
  @Query(
      "DELETE FROM ProductOptionGroupEntity pog "
          + "WHERE pog.id.productId = :productId AND pog.id.optionGroupId = :optionGroupId")
  void deleteByProductIdAndOptionGroupId(
      @Param("productId") Long productId, @Param("optionGroupId") Long optionGroupId);

  /** Removes all product associations for the given option group. */
  @Modifying
  @Query("DELETE FROM ProductOptionGroupEntity pog WHERE pog.id.optionGroupId = :optionGroupId")
  void deleteByOptionGroupId(@Param("optionGroupId") Long optionGroupId);

  /** Projection used by {@link #findProductIdsByOptionGroupIds}. */
  interface ProductIdProjection {
    Long getOptionGroupId();

    Long getProductId();
  }

  /** Projection for the per-product required flag of an option group. */
  interface GroupRequiredProjection {
    Long getOptionGroupId();

    Boolean getRequired();
  }
}
