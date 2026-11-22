/* (C) 2026 */

package aros.services.rms.core.analytics.application.service;

import aros.services.rms.core.analytics.domain.AnalyticsConfig;
import aros.services.rms.core.analytics.domain.AnalyticsPeriods;
import aros.services.rms.core.analytics.domain.OperationsReport;
import aros.services.rms.core.analytics.domain.OperationsReport.DayPartTurnover;
import aros.services.rms.core.analytics.domain.OperationsReport.LaborShare;
import aros.services.rms.core.analytics.domain.OperationsReport.PeriodInfo;
import aros.services.rms.core.analytics.domain.OperationsReport.RevPash;
import aros.services.rms.core.analytics.domain.OperationsReport.TableTurnover;
import aros.services.rms.core.analytics.domain.port.in.GetOperationsUseCase;
import aros.services.rms.core.analytics.domain.port.out.AnalyticsConfigRepositoryPort;
import aros.services.rms.core.analytics.domain.port.out.OperationsQueryPort;
import aros.services.rms.core.analytics.domain.port.out.OperationsQueryPort.DayPartCovers;
import aros.services.rms.core.analytics.domain.port.out.OperationsQueryPort.OperationsSnapshot;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Computes the operational efficiency report from the seeded order, table and rollup data.
 *
 * <p>Definitions (see {@code docs/contracts/analytics.md}):
 *
 * <ul>
 *   <li>{@code occupiedMinutes = closeTime - openTime} per order.
 *   <li>{@code occupiedSeatMinutes = occupiedMinutes × covers}.
 *   <li>{@code availableSeatHours = seatCount × operatingMinutes × days / 60}.
 *   <li>{@code RevPASH = netSales / availableSeatHours}.
 *   <li>{@code turns = covers / tableCount} overall and per day part.
 *   <li>{@code laborShare = laborCost / netSales}.
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class GetOperationsService implements GetOperationsUseCase {

  private final OperationsQueryPort operationsQueryPort;
  private final AnalyticsConfigRepositoryPort configRepositoryPort;
  private final CurrencyProvider currencyProvider;

  /** {@inheritDoc} */
  @Override
  public OperationsReport execute(String bucket, String from, String to) {
    final List<String> keys = AnalyticsPeriods.validateAndGenerate(bucket, from, to);
    LocalDate start = AnalyticsPeriods.start(bucket, from);
    LocalDate endExclusive = AnalyticsPeriods.endExclusive(bucket, to);
    AnalyticsConfig config = configRepositoryPort.findSingleton();

    OperationsSnapshot snapshot =
        operationsQueryPort.load(
            start,
            endExclusive,
            config.lunchStart(),
            config.lunchEnd(),
            config.dinnerStart(),
            config.dinnerEnd());
    Money laborCost = operationsQueryPort.loadLaborCost(start, endExclusive);

    Currency currency = currencyProvider.getCurrency();
    Money zero = Money.zero(currency);

    long days = Math.max(1, ChronoUnit.DAYS.between(start, endExclusive));
    long operatingMinutes =
        Math.max(0L, Duration.between(config.defaultOpen(), config.defaultClose()).toMinutes());
    BigDecimal availableSeatHours =
        BigDecimal.valueOf(snapshot.seatCount())
            .multiply(BigDecimal.valueOf(operatingMinutes))
            .multiply(BigDecimal.valueOf(days))
            .divide(new BigDecimal("60"), 2, RoundingMode.HALF_UP);

    final Money revPash =
        availableSeatHours.signum() == 0
            ? zero
            : new Money(
                snapshot.netSales().amount().divide(availableSeatHours, 2, RoundingMode.HALF_UP),
                currency);

    final BigDecimal turns =
        snapshot.tableCount() == 0
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(snapshot.covers())
                .divide(BigDecimal.valueOf(snapshot.tableCount()), 2, RoundingMode.HALF_UP);

    List<DayPartTurnover> byDayPart = new ArrayList<>();
    for (DayPartCovers dayPart : snapshot.coversByDayPart()) {
      BigDecimal dayPartTurns =
          snapshot.tableCount() == 0
              ? BigDecimal.ZERO
              : BigDecimal.valueOf(dayPart.covers())
                  .divide(BigDecimal.valueOf(snapshot.tableCount()), 2, RoundingMode.HALF_UP);
      byDayPart.add(
          new DayPartTurnover(
              dayPart.dayPart(), dayPartTurns, dayPart.covers(), snapshot.tableCount()));
    }
    byDayPart.sort(java.util.Comparator.comparingInt(dayPart -> dayPartOrder(dayPart.dayPart())));

    BigDecimal avgOccupancyMinutes =
        snapshot.ordersWithTimestamps() == 0
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(snapshot.occupiedMinutes())
                .divide(
                    BigDecimal.valueOf(snapshot.ordersWithTimestamps()), 2, RoundingMode.HALF_UP);

    BigDecimal laborSharePct =
        snapshot.netSales().isZero()
            ? BigDecimal.ZERO
            : laborCost
                .amount()
                .multiply(new BigDecimal("100"))
                .divide(snapshot.netSales().amount(), 2, RoundingMode.HALF_UP);

    List<String> notes = new ArrayList<>();
    String completeness;
    if (snapshot.totalOrders() == 0) {
      completeness = "EMPTY";
      notes.add("No delivered orders found for the requested range.");
    } else if (snapshot.ordersWithTimestamps() < snapshot.totalOrders()) {
      completeness = "PARTIAL";
      notes.add(
          (snapshot.totalOrders() - snapshot.ordersWithTimestamps())
              + " order(s) lack open/close timestamps and were excluded from occupancy metrics.");
    } else {
      completeness = "FULL";
    }
    if (laborCost.isZero() && snapshot.totalOrders() > 0) {
      notes.add(
          "Labor cost is zero for the range because no daily financial rollup row is available.");
    }

    return new OperationsReport(
        new PeriodInfo(bucket, from, to, keys),
        new RevPash(
            revPash, snapshot.netSales() == null ? zero : snapshot.netSales(), availableSeatHours),
        new TableTurnover(turns, byDayPart),
        new LaborShare(laborCost, laborSharePct),
        snapshot.covers(),
        snapshot.totalOrders(),
        avgOccupancyMinutes,
        snapshot.occupiedSeatMinutes(),
        completeness,
        notes);
  }

  private static int dayPartOrder(String dayPart) {
    return switch (dayPart) {
      case "LUNCH" -> 0;
      case "DINNER" -> 1;
      default -> 2;
    };
  }
}
