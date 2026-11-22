/* (C) 2026 */

package aros.services.rms.core.payment.domain;

import java.util.List;

/**
 * One page of payments.
 *
 * @param items the page items
 * @param total total matching payments
 * @param page zero-based page number
 * @param size page size
 */
public record PaymentQueryResult(List<Payment> items, long total, int page, int size) {}
