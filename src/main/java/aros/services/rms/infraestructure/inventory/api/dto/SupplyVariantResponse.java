/* (C) 2026 */
package aros.services.rms.infraestructure.inventory.api.dto;

import aros.services.rms.infraestructure.inventory.persistence.SupplyVariantEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/** Response DTO for a supply variant (presentación física del insumo). */
@Schema(description = "Supply variant data with current stock levels per location")
public record SupplyVariantResponse(
    @Schema(description = "Variant ID", example = "7") Long id,
    @Schema(description = "Supply ID", example = "3") Long supplyId,
    @Schema(description = "Supply name", example = "Carne de Res") String supplyName,
    @Schema(description = "Category ID", example = "1") Long categoryId,
    @Schema(description = "Category name", example = "Proteínas") String categoryName,
    @Schema(description = "Unit of measure ID", example = "2") Long unitId,
    @Schema(description = "Unit abbreviation", example = "kg") String unitAbbreviation,
    @Schema(description = "Quantity per unit", example = "1.000") BigDecimal quantity,
    @Schema(description = "Current stock in Bodega", example = "18.500") BigDecimal stockBodega,
    @Schema(description = "Current stock in Cocina", example = "5.000") BigDecimal stockCocina) {

  public static SupplyVariantResponse fromEntity(SupplyVariantEntity entity) {
    return fromEntity(entity, BigDecimal.ZERO, BigDecimal.ZERO);
  }

  public static SupplyVariantResponse fromEntity(
      SupplyVariantEntity entity, BigDecimal stockBodega, BigDecimal stockCocina) {
    if (entity == null) return null;
    var supply = entity.getSupply();
    var unit = entity.getUnit();
    return new SupplyVariantResponse(
        entity.getId(),
        supply != null ? supply.getId() : null,
        supply != null ? supply.getName() : null,
        supply != null && supply.getCategory() != null ? supply.getCategory().getId() : null,
        supply != null && supply.getCategory() != null ? supply.getCategory().getName() : null,
        unit != null ? unit.getId() : null,
        unit != null ? unit.getAbbreviation() : null,
        entity.getQuantity(),
        stockBodega,
        stockCocina);
  }
}
