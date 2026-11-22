/* (C) 2026 */

package aros.services.rms.core.payment.application.service;

import aros.services.rms.core.cashshift.application.exception.CashShiftNotFoundException;
import aros.services.rms.core.cashshift.application.exception.ShiftClosedException;
import aros.services.rms.core.cashshift.domain.CashShift;
import aros.services.rms.core.cashshift.domain.CashShiftStatus;
import aros.services.rms.core.cashshift.port.output.CashShiftRepositoryPort;
import aros.services.rms.core.payment.application.dto.VoidPaymentCommand;
import aros.services.rms.core.payment.application.exception.InvalidPaymentException;
import aros.services.rms.core.payment.application.exception.PaymentAlreadyVoidedException;
import aros.services.rms.core.payment.application.exception.PaymentNotFoundException;
import aros.services.rms.core.payment.domain.Payment;
import aros.services.rms.core.payment.domain.PaymentStatus;
import aros.services.rms.core.payment.port.input.VoidPaymentUseCase;
import aros.services.rms.core.payment.port.output.PaymentRepositoryPort;
import java.time.LocalDateTime;

/**
 * Voids a completed payment.
 *
 * <p>A void is all-or-nothing, requires a reason, keeps the row and is only allowed while the
 * owning shift is open. The use case name is what the audit aspect records as the business action.
 */
public class VoidPaymentService implements VoidPaymentUseCase {

  private static final int MAX_REASON_LENGTH = 255;

  private final PaymentRepositoryPort paymentRepositoryPort;
  private final CashShiftRepositoryPort cashShiftRepositoryPort;

  /**
   * Creates the service.
   *
   * @param paymentRepositoryPort payment persistence port
   * @param cashShiftRepositoryPort cash-shift persistence port
   */
  public VoidPaymentService(
      PaymentRepositoryPort paymentRepositoryPort,
      CashShiftRepositoryPort cashShiftRepositoryPort) {
    this.paymentRepositoryPort = paymentRepositoryPort;
    this.cashShiftRepositoryPort = cashShiftRepositoryPort;
  }

  /** {@inheritDoc} */
  @Override
  public Payment voidPayment(VoidPaymentCommand command) {
    if (command == null || command.paymentId() == null) {
      throw new InvalidPaymentException("A paymentId is required");
    }
    final String reason = normalizeReason(command.reason());

    Payment payment =
        paymentRepositoryPort
            .findById(command.paymentId())
            .orElseThrow(() -> new PaymentNotFoundException(command.paymentId()));

    if (payment.getStatus() == PaymentStatus.VOIDED) {
      throw new PaymentAlreadyVoidedException(payment.getId());
    }

    CashShift shift =
        cashShiftRepositoryPort
            .findById(payment.getShiftId())
            .orElseThrow(() -> new CashShiftNotFoundException(payment.getShiftId()));
    if (shift.getStatus() != CashShiftStatus.OPEN) {
      throw new ShiftClosedException(shift.getId());
    }

    payment.setStatus(PaymentStatus.VOIDED);
    payment.setVoidedAt(LocalDateTime.now());
    payment.setVoidedBy(command.voidedBy());
    payment.setVoidReason(reason);
    return paymentRepositoryPort.save(payment);
  }

  private static String normalizeReason(String reason) {
    if (reason == null || reason.isBlank()) {
      throw new InvalidPaymentException("A void reason is required");
    }
    String normalized = reason.trim();
    if (normalized.length() > MAX_REASON_LENGTH) {
      throw new InvalidPaymentException(
          "The void reason must not exceed " + MAX_REASON_LENGTH + " characters");
    }
    return normalized;
  }
}
