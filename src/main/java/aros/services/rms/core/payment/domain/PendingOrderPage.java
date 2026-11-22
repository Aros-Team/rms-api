/* (C) 2026 */

package aros.services.rms.core.payment.domain;

import java.util.List;

/**
 * One page of pending orders.
 *
 * @param items the page items
 * @param total total matching orders
 * @param page zero-based page number
 * @param size page size
 */
public record PendingOrderPage(List<PendingOrder> items, long total, int page, int size) {}
