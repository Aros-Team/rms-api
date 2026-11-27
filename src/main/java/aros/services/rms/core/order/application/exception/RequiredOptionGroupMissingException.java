/* (C) 2026 */

package aros.services.rms.core.order.application.exception;

import java.util.List;

/**
 * Exception thrown when an order detail omits a selection for an option group marked as required
 * for the product.
 *
 * <p>The group is mandatory because the product's workflow needs the answer (for example a protein
 * choice or an explicit "with/without onion"). The API rejects the order instead of silently
 * sending incomplete information to the kitchen.
 *
 * <p>Maps to HTTP 400 via {@code GlobalExceptionHandler}.
 */
public class RequiredOptionGroupMissingException extends RuntimeException {

  private final String productName;
  private final List<String> missingGroupNames;

  /**
   * Creates the exception with the product and the groups that still need a selection.
   *
   * @param productName the product being ordered
   * @param missingGroupNames human-readable names of the missing required groups
   */
  public RequiredOptionGroupMissingException(String productName, List<String> missingGroupNames) {
    super(
        "El producto '"
            + productName
            + "' requiere elegir una opción en: "
            + String.join(", ", missingGroupNames));
    this.productName = productName;
    this.missingGroupNames = List.copyOf(missingGroupNames);
  }

  /**
   * Returns the product that failed validation.
   *
   * @return the product name
   */
  public String getProductName() {
    return productName;
  }

  /**
   * Returns the names of the required groups without a selection.
   *
   * @return the missing group names
   */
  public List<String> getMissingGroupNames() {
    return missingGroupNames;
  }
}
