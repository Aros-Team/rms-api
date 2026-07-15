/* (C) 2026 */

package aros.services.rms.core.common.money.application;

import aros.services.rms.core.common.money.domain.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Pure static helpers for common monetary operations. No Spring dependencies. */
public final class MoneyCalculator {

  private MoneyCalculator() {}

  /**
   * Calculates the weighted average of two Money values by their quantities.
   *
   * @param current the current Money value
   * @param currentQty the quantity for the current value
   * @param purchased the purchased Money value
   * @param purchasedQty the quantity for the purchased value
   * @return the weighted average, with intermediate scale 6 and HALF_UP rounding
   */
  public static Money weightedAverage(
      Money current, BigDecimal currentQty, Money purchased, BigDecimal purchasedQty) {
    Money totalValue = current.times(currentQty).plus(purchased.times(purchasedQty));
    BigDecimal totalQty = currentQty.add(purchasedQty);
    return totalValue.divide(totalQty, 6, RoundingMode.HALF_UP);
  }

  /**
   * Splits a total evenly into parts using the largest-remainder method.
   *
   * @param total the total amount to split
   * @param parts the number of parts
   * @return a list of allocated amounts summing to the total
   */
  public static List<Money> splitEvenly(Money total, int parts) {
    return total.allocate(parts);
  }

  /**
   * Applies a percentage to a base amount. Delegates to {@link Money#percent(BigDecimal)}.
   *
   * @param base the base amount
   * @param pct the percentage to apply
   * @return the computed percentage amount
   */
  public static Money applyPercentage(Money base, BigDecimal pct) {
    return base.percent(pct);
  }

  /**
   * Calculates the labor cost for a given preparation time and hourly rate.
   *
   * @param prepMinutes the preparation time in minutes
   * @param costPerHour the hourly labor cost
   * @return the labor cost for the given minutes
   */
  public static Money laborCost(int prepMinutes, Money costPerHour) {
    BigDecimal hours =
        BigDecimal.valueOf(prepMinutes).divide(BigDecimal.valueOf(60), 6, RoundingMode.HALF_UP);
    return costPerHour.times(hours, RoundingMode.HALF_UP);
  }
}
