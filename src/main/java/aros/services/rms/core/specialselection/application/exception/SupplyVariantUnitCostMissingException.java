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
        "Cannot compute suggested price: unit_cost is missing for supply_variant IDs: "
            + variantIds);
    this.variantIds = variantIds;
  }

  public List<Long> getVariantIds() {
    return variantIds;
  }
}
