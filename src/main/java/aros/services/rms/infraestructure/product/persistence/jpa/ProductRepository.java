/* (C) 2026 */

package aros.services.rms.infraestructure.product.persistence.jpa;

import aros.services.rms.infraestructure.product.persistence.Product;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for Product entities. */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
  /** Finds products by category IDs. */
  List<Product> findByCategoryIdIn(List<Long> categoryIds);

  /** Finds all active products. */
  @Query("SELECT p FROM Product p WHERE p.active = true")
  Page<Product> findAllActive(@Param("active") boolean active, Pageable pageable);

  /** Finds all products with the standard selection type. */
  @Query("SELECT p FROM Product p WHERE p.selectionType = 'STANDARD'")
  List<Product> findAllStandard();

  /** Finds all standard selection type products with pagination. */
  @Query("SELECT p FROM Product p WHERE p.selectionType = 'STANDARD'")
  Page<Product> findAllStandard(Pageable pageable);

  /** Finds standard selection type products within the given categories. */
  @Query(
      "SELECT p FROM Product p WHERE p.selectionType = 'STANDARD' AND p.category.id IN"
          + " :categoryIds")
  List<Product> findByCategoryIdInStandard(@Param("categoryIds") List<Long> categoryIds);

  /**
   * Searches products by partial case-insensitive match against name, description, or
   * category.name, combining optional category / active / selection filters in the DB.
   *
   * @param search the partial, case-insensitive term (wrapped in {@code %...%})
   * @param categoryIds optional category filter; pass {@code null} to skip
   * @param includeInactive when {@code true}, inactive products are also returned
   * @param includeSelections when {@code true}, special selection products are also returned
   * @param pageable pagination parameters
   * @return page of matching products
   */
  @Query(
      "SELECT p FROM Product p LEFT JOIN p.category c "
          + "WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) "
          + "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')) "
          + "OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))) "
          + "AND (:includeInactive = true OR p.active = true) "
          + "AND (:includeSelections = true OR p.selectionType = 'STANDARD') "
          + "AND (:categoryIds IS NULL OR c.id IN :categoryIds)")
  Page<Product> searchByNameOrDescriptionOrCategoryName(
      @Param("search") String search,
      @Param("categoryIds") List<Long> categoryIds,
      @Param("includeInactive") boolean includeInactive,
      @Param("includeSelections") boolean includeSelections,
      Pageable pageable);
}
