/* (C) 2026 */

package aros.services.rms.core.payment.application.dto;

import aros.services.rms.core.payment.domain.PaymentStatus;
import aros.services.rms.core.payment.domain.PaymentTenderMethod;
import java.time.LocalDate;

/**
 * Filter for the paged payment journal.
 *
 * @param date business date of {@code completedAt}, or {@code null}
 * @param method tender method to require, or {@code null}
 * @param status payment status filter, or {@code null}
 * @param page zero-based page number
 * @param size page size
 */
public record PaymentFilter(
    LocalDate date, PaymentTenderMethod method, PaymentStatus status, int page, int size) {}
