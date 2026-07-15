/* (C) 2026 */

package aros.services.rms.core.common.money.domain.exception;

import java.util.Currency;

/** Exception thrown when an operation involves two different currencies. */
public class CurrencyMismatchException extends RuntimeException {

  private final Currency expected;
  private final Currency actual;

  /**
   * Creates a new exception for currency mismatch.
   *
   * @param expected the expected currency
   * @param actual the actual currency found
   */
  public CurrencyMismatchException(Currency expected, Currency actual) {
    super("Currency mismatch: expected " + expected + " but was " + actual);
    this.expected = expected;
    this.actual = actual;
  }

  /**
   * Returns the expected currency.
   *
   * @return the expected currency
   */
  public Currency getExpectedCurrency() {
    return expected;
  }

  /**
   * Returns the actual currency that was found.
   *
   * @return the actual currency
   */
  public Currency getActualCurrency() {
    return actual;
  }
}
