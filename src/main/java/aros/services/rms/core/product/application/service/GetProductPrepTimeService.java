/* (C) 2026 */

package aros.services.rms.core.product.application.service;

import aros.services.rms.core.product.application.exception.ProductNotFoundException;
import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.domain.ProductPrepTime;
import aros.services.rms.core.product.port.input.GetProductPrepTimeUseCase;
import aros.services.rms.core.product.port.output.ProductPrepTimeStatsPort;
import aros.services.rms.core.product.port.output.ProductRepositoryPort;
import lombok.RequiredArgsConstructor;

/** Resolves estimated versus measured preparation time for a product. */
@RequiredArgsConstructor
public class GetProductPrepTimeService implements GetProductPrepTimeUseCase {

  private final ProductRepositoryPort productRepositoryPort;
  private final ProductPrepTimeStatsPort productPrepTimeStatsPort;

  /** {@inheritDoc} */
  @Override
  public ProductPrepTime get(Long productId) {
    Product product =
        productRepositoryPort
            .findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
    return ProductPrepTime.resolve(
        product.getEstimatedPrepMinutes(),
        productPrepTimeStatsPort.findByProductId(productId),
        product.getPrepTimeMode());
  }
}
