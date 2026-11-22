/* (C) 2026 */

package aros.services.rms.core.cashshift.domain;

/**
 * A cash shift together with its derived totals.
 *
 * @param shift the shift header
 * @param totals the totals computed from payments and movements
 */
public record CashShiftSummary(CashShift shift, CashShiftTotals totals) {}
