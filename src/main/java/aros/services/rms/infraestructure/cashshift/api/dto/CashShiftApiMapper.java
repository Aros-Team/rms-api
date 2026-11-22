/* (C) 2026 */

package aros.services.rms.infraestructure.cashshift.api.dto;

import aros.services.rms.core.cashshift.domain.CashShift;
import aros.services.rms.core.cashshift.domain.CashShiftSummary;
import aros.services.rms.core.cashshift.domain.CashShiftTotals;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.infraestructure.common.dto.MoneyDto;
import org.springframework.stereotype.Component;

/** Maps cash-shift domain objects to the wire DTOs. */
@Component
public class CashShiftApiMapper {

  /**
   * Builds the shift header response.
   *
   * @param shift the domain shift
   * @return the response DTO
   */
  public CashShiftResponse toResponse(CashShift shift) {
    return new CashShiftResponse(
        shift.getId(),
        shift.getStatus() != null ? shift.getStatus().name() : null,
        shift.getOpeningFloat().currency().getCurrencyCode(),
        shift.getOpenedBy(),
        shift.getOpenedAt(),
        MoneyDto.of(shift.getOpeningFloat()),
        shift.getNotes());
  }

  /**
   * Builds the Z-summary response.
   *
   * @param summary the shift and its totals
   * @return the summary DTO
   */
  public CashShiftSummaryResponse toSummary(CashShiftSummary summary) {
    CashShift shift = summary.shift();
    CashShiftTotals totals = summary.totals();
    Money expectedCash =
        shift.getExpectedCash() != null ? shift.getExpectedCash() : totals.expectedCash();
    return new CashShiftSummaryResponse(
        shift.getId(),
        shift.getStatus() != null ? shift.getStatus().name() : null,
        shift.getOpeningFloat().currency().getCurrencyCode(),
        shift.getOpenedBy(),
        shift.getOpenedAt(),
        shift.getClosedBy(),
        shift.getClosedAt(),
        MoneyDto.of(shift.getOpeningFloat()),
        totals.paymentsCount(),
        totals.voidedCount(),
        MoneyDto.of(totals.subtotal()),
        MoneyDto.of(totals.discounts()),
        MoneyDto.of(totals.tips()),
        MoneyDto.of(totals.totalCharged()),
        MoneyDto.of(totals.cash()),
        MoneyDto.of(totals.card()),
        MoneyDto.of(totals.transfer()),
        MoneyDto.of(totals.changeGiven()),
        MoneyDto.of(totals.paidIn()),
        MoneyDto.of(totals.paidOut()),
        MoneyDto.of(expectedCash),
        MoneyDto.of(shift.getCountedCash()),
        MoneyDto.of(shift.getDifference()),
        shift.getNotes());
  }
}
