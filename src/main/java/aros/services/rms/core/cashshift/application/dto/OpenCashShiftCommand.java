/* (C) 2026 */

package aros.services.rms.core.cashshift.application.dto;

import aros.services.rms.core.common.money.domain.Money;

/**
 * Command to open a cash shift.
 *
 * @param openingFloat the cash float in the drawer
 * @param openedBy the authenticated user opening the shift
 */
public record OpenCashShiftCommand(Money openingFloat, Long openedBy) {}
