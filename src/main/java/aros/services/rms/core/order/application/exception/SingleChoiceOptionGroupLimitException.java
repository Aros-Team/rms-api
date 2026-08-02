/* (C) 2026 */

package aros.services.rms.core.order.application.exception;

/**
 * Exception thrown when an order detail selects more than one option from a {@code SINGLE_CHOICE}
 * option category.
 *
 * <p>A {@code SINGLE_CHOICE} category declares that exactly one of its options may be chosen (e.g.
 * "Proteína": pick exactly one). Selecting two or more options from the same category is rejected
 * at order-taking time.
 *
 * <p>Maps to HTTP 400 via {@code GlobalExceptionHandler}.
 */
public class SingleChoiceOptionGroupLimitException extends RuntimeException {

  private final Long categoryId;
  private final int selectedCount;

  /**
   * Creates a new exception with the conflicting category and selected option count.
   *
   * @param categoryId the option category identifier
   * @param categoryName the option category name (cosmetic; may be null)
   * @param selectedCount the number of options selected from this category
   */
  public SingleChoiceOptionGroupLimitException(
      Long categoryId, String categoryName, int selectedCount) {
    super(
        "SINGLE_CHOICE category limit exceeded: '"
            + (categoryName == null ? categoryId : categoryName)
            + "' (categoryId="
            + categoryId
            + ") allows at most 1 option, but "
            + selectedCount
            + " were selected");
    this.categoryId = categoryId;
    this.selectedCount = selectedCount;
  }

  /**
   * Returns the option category identifier that was violated.
   *
   * @return the category id
   */
  public Long getCategoryId() {
    return categoryId;
  }

  /**
   * Returns the number of options that were selected from the violated category.
   *
   * @return the number of selected options
   */
  public int getSelectedCount() {
    return selectedCount;
  }
}
