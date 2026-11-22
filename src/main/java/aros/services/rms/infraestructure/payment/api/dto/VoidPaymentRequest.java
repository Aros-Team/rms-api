/* (C) 2026 */

package aros.services.rms.infraestructure.payment.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for a void.
 *
 * @param reason the mandatory void reason
 */
public record VoidPaymentRequest(@NotBlank @Size(max = 255) String reason) {}
