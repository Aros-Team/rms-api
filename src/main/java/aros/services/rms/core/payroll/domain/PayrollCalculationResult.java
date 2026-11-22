/* (C) 2026 */

package aros.services.rms.core.payroll.domain;

import aros.services.rms.core.common.money.domain.Money;
import java.math.BigDecimal;

/** Result of a payroll event rate calculation. */
public record PayrollCalculationResult(
    Long userId,
    String eventType,
    Money hourlyRate,
    BigDecimal multiplier,
    Money suggestedUnitRate,
    Money suggestedAmount) {}
