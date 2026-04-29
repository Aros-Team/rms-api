/* (C) 2026 */

package aros.services.rms.infraestructure.product.persistence.jpa;

import aros.services.rms.infraestructure.product.persistence.ProductOption;
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

  /** Removes all options from a product. */
  @Modifying
  @Query(
      value = "DELETE FROM product_product_options WHERE product_id = :productId",
      nativeQuery = true)
  void removeAllOptionsFromProduct(@Param("productId") Long productId);

  /** Associates an option to a product. */
  @Modifying
  @Query(
      value =
          "INSERT INTO product_product_options (product_id, option_id) "
              + "VALUES (:productId, :optionId)",
      nativeQuery = true)
  void associateOptionToProduct(
      @Param("productId") Long productId, @Param("optionId") Long optionId);

  /** Checks if an option is associated with a product. */
  @Query(
      value =
          "SELECT COUNT(*) FROM product_product_options "
              + "WHERE product_id = :productId AND option_id = :optionId",
      nativeQuery = true)
  Long isOptionAssociatedWithProduct(
      @Param("productId") Long productId, @Param("optionId") Long optionId);
}
