/* (C) 2026 */

package aros.services.rms.core.payment.application.dto;

/**
 * Command to void a completed payment.
 *
 * @param paymentId the payment to void
 * @param reason the mandatory void reason
 * @param voidedBy the authenticated user performing the void
 */
public record VoidPaymentCommand(Long paymentId, String reason, Long voidedBy) {}
