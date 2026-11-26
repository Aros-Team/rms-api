package aros.services.rms.core.specialselection.application.exception;

import java.util.List;

/**
 * Exception raised when the suggested price cannot be computed because one or more referenced
 * supply variants do not have a unit cost defined.
 */
public class SupplyVariantUnitCostMissingException extends RuntimeException {
  private final List<Long> variantIds;

  /**
   * Creates a new supply variant unit cost missing exception.
   *
   * @param variantIds the supply variant identifiers missing a unit cost
   */
  public SupplyVariantUnitCostMissingException(List<Long> variantIds) {
    super(
        "No se puede calcular el precio sugerido: falta unit_cost para los IDs "
            + "de variante de insumo: "
            + variantIds);
    this.variantIds = variantIds;
  }

  public List<Long> getVariantIds() {
    return variantIds;
  }
}
