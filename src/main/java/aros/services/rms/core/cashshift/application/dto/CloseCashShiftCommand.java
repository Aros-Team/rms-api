/* (C) 2026 */

package aros.services.rms.core.cashshift.application.dto;

import aros.services.rms.core.common.money.domain.Money;

/**
 * Command to close a cash shift.
 *
 * @param shiftId the shift to close
 * @param countedCash the cash counted by the cashier
 * @param notes optional close notes
 * @param closedBy the authenticated user closing the shift
 */
public record CloseCashShiftCommand(Long shiftId, Money countedCash, String notes, Long closedBy) {}
