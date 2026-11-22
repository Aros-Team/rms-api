/* (C) 2026 */

package aros.services.rms.core.payment.domain;

/**
 * Result of a registration attempt.
 *
 * @param payment the stored payment
 * @param replayed {@code true} when an existing payment was returned for the same idempotency key
 */
public record RegisteredPayment(Payment payment, boolean replayed) {}
