/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain;

import aros.services.rms.core.common.money.domain.Money;

/**
 * Typed labor costing configuration.
 *
 * @param mode the costing strategy
 * @param standardHourlyRate fallback rate used when payroll data is unavailable
 * @param defaultExpectedHoursPerMonth expected hours used when a worker has none configured
 */
public record LaborSettings(
    LaborCostMode mode, Money standardHourlyRate, int defaultExpectedHoursPerMonth) {}
