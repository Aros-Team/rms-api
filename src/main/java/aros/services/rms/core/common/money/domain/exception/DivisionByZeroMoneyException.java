/* (C) 2026 */

package aros.services.rms.core.common.money.domain.exception;

import java.util.Currency;

/** Exception thrown when attempting to divide a Money amount by zero. */
public class DivisionByZeroMoneyException extends RuntimeException {

  private final Currency currency;

  /**
   * Creates a new exception for division by zero on a Money amount.
   *
   * @param currency the currency of the Money being divided
   */
  public DivisionByZeroMoneyException(Currency currency) {
    super("Division by zero for currency " + currency);
    this.currency = currency;
  }

  /**
   * Returns the currency of the Money that was being divided.
   *
   * @return the currency
   */
  public Currency getCurrency() {
    return currency;
  }
}
