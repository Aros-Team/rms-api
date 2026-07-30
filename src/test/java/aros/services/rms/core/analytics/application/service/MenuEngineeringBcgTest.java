/* (C) 2026 */

package aros.services.rms.core.analytics.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import aros.services.rms.core.analytics.domain.BcgQuadrant;
import aros.services.rms.core.analytics.domain.MenuEngineeringReport.MenuItemSummary;
import aros.services.rms.core.common.money.domain.Money;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Pure unit tests for the BCG math logic ({@link RefreshMenuEngineeringService#assignQuadrant},
 * median). No mocking needed.
 */
class MenuEngineeringBcgTest {

  private static final Currency COP = Currency.getInstance("COP");
  private static final Money ZERO = Money.zero(COP);

  // ---------------------------------------------------------------------------
  // assignQuadrant
  // ---------------------------------------------------------------------------

  @Test
  void star() {
    assertQuadrant(BcgQuadrant.STAR, 100, usd("40.00"), 50, usd("30.00"));
  }

  @Test
  void plowhorse() {
    assertQuadrant(BcgQuadrant.PLOWHORSE, 100, usd("20.00"), 50, usd("30.00"));
  }

  @Test
  void puzzle() {
    assertQuadrant(BcgQuadrant.PUZZLE, 10, usd("40.00"), 50, usd("30.00"));
  }

  @Test
  void dog() {
    assertQuadrant(BcgQuadrant.DOG, 10, usd("20.00"), 50, usd("30.00"));
  }

  @Test
  void tieHighVolumeAndMargin() {
    assertQuadrant(BcgQuadrant.STAR, 50, usd("30.00"), 50, usd("30.00"));
  }

  // ---------------------------------------------------------------------------
  // medianInt
  // ---------------------------------------------------------------------------

  @Test
  void medianIntOdd() {
    assertEquals(5, RefreshMenuEngineeringService.medianInt(List.of(1, 10, 5)));
  }

  @Test
  void medianIntEvenUpperMiddle() {
    assertEquals(10, RefreshMenuEngineeringService.medianInt(List.of(1, 5, 10, 20)));
  }

  @Test
  void medianIntEmpty() {
    assertEquals(0, RefreshMenuEngineeringService.medianInt(List.of()));
  }

  // ---------------------------------------------------------------------------
  // medianBigDecimal
  // ---------------------------------------------------------------------------

  @Test
  void medianBigDecimalOdd() {
    assertEquals(
        bd("5.00"),
        RefreshMenuEngineeringService.medianBigDecimal(
            List.of(bd("1.00"), bd("10.00"), bd("5.00"))));
  }

  @Test
  void medianBigDecimalEmpty() {
    assertEquals(BigDecimal.ZERO, RefreshMenuEngineeringService.medianBigDecimal(List.of()));
  }

  // ---------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------

  private static BigDecimal bd(String s) {
    return new BigDecimal(s);
  }

  private static Money usd(String s) {
    return new Money(new BigDecimal(s), COP);
  }

  private static void assertQuadrant(
      BcgQuadrant expected, int unitsSold, Money gpPerUnit, int medianVolume, Money medianMargin) {
    MenuItemSummary item =
        new MenuItemSummary(
            1L,
            "T",
            null,
            "",
            unitsSold,
            ZERO, // revenue
            ZERO, // recipeCost
            ZERO, // avgOptionCost
            ZERO, // effectiveCost
            gpPerUnit, // grossProfitPerUnit
            ZERO, // totalContribution
            BcgQuadrant.DOG);
    assertEquals(
        expected, RefreshMenuEngineeringService.assignQuadrant(item, medianVolume, medianMargin));
  }
}
