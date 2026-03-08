/* (C) 2026 */
package aros.services.rms.core.product.port.input;

import aros.services.rms.core.product.domain.ProductOption;
import java.util.List;

/**
 * Input port for product option management. Product options are specific customization choices
 * (e.g., "Medium rare", "Almond milk") linked to an option category.
 */
public interface ProductOptionUseCase {

  ProductOption create(ProductOption productOption);

  ProductOption update(Long id, ProductOption productOption);

  List<ProductOption> findAll();

  ProductOption findById(Long id);
}