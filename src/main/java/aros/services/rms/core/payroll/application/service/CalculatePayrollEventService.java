/* (C) 2026 */

package aros.services.rms.core.payroll.application.service;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payroll.domain.PayrollCalculationResult;
import aros.services.rms.core.payroll.domain.PayrollEventType;
import aros.services.rms.core.payroll.domain.port.input.CalculatePayrollEventUseCase;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

/** Service that calculates the suggested unit rate for a payroll event. */
public class CalculatePayrollEventService implements CalculatePayrollEventUseCase {

  private final UserRepositoryPort userRepositoryPort;
  private final CurrencyProvider currencyProvider;

  /**
   * Creates a new CalculatePayrollEventService.
   *
   * @param userRepositoryPort the user repository port
   * @param currencyProvider the currency provider
   */
  public CalculatePayrollEventService(
      UserRepositoryPort userRepositoryPort, CurrencyProvider currencyProvider) {
    this.userRepositoryPort = userRepositoryPort;
    this.currencyProvider = currencyProvider;
  }

  @Override
  public PayrollCalculationResult calculate(Long userId, String eventType, BigDecimal quantity) {
    PayrollEventType type = PayrollEventType.valueOf(eventType);

    Optional<User> userOpt = userRepositoryPort.findById(UserId.of(userId));
    if (userOpt.isEmpty()) {
      return null;
    }

    User user = userOpt.get();
    Currency currency = currencyProvider.getCurrency();
    Money hourlyRate = user.getHourlyRate(currency);

    BigDecimal multiplier = type.getMultiplier();
    if (multiplier == null) {
      return new PayrollCalculationResult(userId, eventType, hourlyRate.amount(), null, null, null);
    }

    BigDecimal suggestedUnitRate = hourlyRate.amount().multiply(multiplier);
    BigDecimal suggestedAmount = null;
    if (quantity != null) {
      suggestedAmount = suggestedUnitRate.multiply(quantity);
    }

    return new PayrollCalculationResult(
        userId, eventType, hourlyRate.amount(), multiplier, suggestedUnitRate, suggestedAmount);
  }
}
