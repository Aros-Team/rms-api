/* (C) 2026 */

package aros.services.rms.core.analytics.domain;

import aros.services.rms.core.common.money.domain.Money;

/** One product rank in the top-selling report. */
public record TopSellingProduct(
    int rank, Long productId, String productName, long unitsSold, Money revenue) {}
