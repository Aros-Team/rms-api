/* (C) 2026 */

package aros.services.rms.core.payroll.domain;

import java.math.BigDecimal;

/** Result of a payroll event rate calculation. */
public record PayrollCalculationResult(
    Long userId,
    String eventType,
    BigDecimal hourlyRate,
    BigDecimal multiplier,
    BigDecimal suggestedUnitRate,
    BigDecimal suggestedAmount) {}
