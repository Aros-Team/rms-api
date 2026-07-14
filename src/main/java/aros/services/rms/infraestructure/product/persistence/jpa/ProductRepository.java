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
}
