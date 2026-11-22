/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain.event;

import java.util.Currency;

/**
 * Published when the system currency changes so caches can be refreshed.
 *
 * @param currency the newly configured currency
 */
public record CurrencyChangedEvent(Currency currency) {}
