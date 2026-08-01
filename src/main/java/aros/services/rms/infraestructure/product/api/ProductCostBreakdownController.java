/* (C) 2026 */

package aros.services.rms.infraestructure.product.api;

import aros.services.rms.core.product.domain.ProductCostBreakdown;
import aros.services.rms.core.product.port.input.GetProductCostBreakdownUseCase;
import aros.services.rms.infraestructure.product.api.dto.ProductCostBreakdownResponse;
import aros.services.rms.infraestructure.product.api.mapper.ProductCostBreakdownResponseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for product material-cost projections. */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product material-cost projection operations")
public class ProductCostBreakdownController {

  private final GetProductCostBreakdownUseCase getProductCostBreakdownUseCase;
  private final ProductCostBreakdownResponseMapper responseMapper;

  /**
   * Gets the base and projected option costs for a product.
   *
   * @param id product identifier
   * @return product cost breakdown
   */
  @Operation(
      tags = {"Products"},
      summary = "Get product cost breakdown",
      description =
          "Returns base recipe material cost, per-option costs, and projected category"
              + " contributions.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Cost breakdown retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found")
      })
  @GetMapping("/{id}/cost-breakdown")
  public ResponseEntity<ProductCostBreakdownResponse> getCostBreakdown(
      @Parameter(description = "Product ID", example = "1", required = true) @PathVariable
          Long id) {
    ProductCostBreakdown breakdown = getProductCostBreakdownUseCase.execute(id);
    return ResponseEntity.ok(responseMapper.toResponse(breakdown));
  }
}
