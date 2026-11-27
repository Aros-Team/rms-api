/* (C) 2026 */

package aros.services.rms.core.payment.application.service;

import aros.services.rms.core.cashshift.application.exception.ShiftNotOpenException;
import aros.services.rms.core.cashshift.domain.CashShift;
import aros.services.rms.core.cashshift.port.output.CashShiftRepositoryPort;
import aros.services.rms.core.order.application.exception.OrderNotFoundException;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.core.payment.application.dto.RegisterPaymentCommand;
import aros.services.rms.core.payment.application.exception.InvalidPaymentException;
import aros.services.rms.core.payment.application.exception.OrderAlreadyPaidException;
import aros.services.rms.core.payment.application.exception.OrderNotPayableException;
import aros.services.rms.core.payment.domain.PayableOrder;
import aros.services.rms.core.payment.domain.Payment;
import aros.services.rms.core.payment.domain.PaymentCalculation;
import aros.services.rms.core.payment.domain.PaymentCalculator;
import aros.services.rms.core.payment.domain.PaymentStatus;
import aros.services.rms.core.payment.domain.PaymentTender;
import aros.services.rms.core.payment.domain.PaymentTenderInput;
import aros.services.rms.core.payment.domain.RegisteredPayment;
import aros.services.rms.core.payment.port.input.RegisterPaymentUseCase;
import aros.services.rms.core.payment.port.output.PayableOrderPort;
import aros.services.rms.core.payment.port.output.PaymentRepositoryPort;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Charges a delivered order inside the open cash shift.
 *
 * <p>The order row is locked before any validation, so two terminals cannot charge the same order
 * concurrently. An idempotency key replays the original payment instead of creating a second one.
 */
public class RegisterPaymentService implements RegisterPaymentUseCase {

  private static final int MAX_IDEMPOTENCY_KEY_LENGTH = 100;

  private final PayableOrderPort payableOrderPort;
  private final PaymentRepositoryPort paymentRepositoryPort;
  private final CashShiftRepositoryPort cashShiftRepositoryPort;

  /**
   * Creates the service.
   *
   * @param payableOrderPort order snapshot port with row locking
   * @param paymentRepositoryPort payment persistence port
   * @param cashShiftRepositoryPort cash-shift persistence port
   */
  public RegisterPaymentService(
      PayableOrderPort payableOrderPort,
      PaymentRepositoryPort paymentRepositoryPort,
      CashShiftRepositoryPort cashShiftRepositoryPort) {
    this.payableOrderPort = payableOrderPort;
    this.paymentRepositoryPort = paymentRepositoryPort;
    this.cashShiftRepositoryPort = cashShiftRepositoryPort;
  }

  /** {@inheritDoc} */
  @Override
  public RegisteredPayment register(RegisterPaymentCommand command) {
    if (command == null || command.orderId() == null) {
      throw new InvalidPaymentException("El orderId es obligatorio");
    }
    String idempotencyKey = normalizeKey(command.idempotencyKey());

    PayableOrder order =
        payableOrderPort
            .findForUpdate(command.orderId())
            .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

    Optional<Payment> replay = paymentRepositoryPort.findByIdempotencyKey(idempotencyKey);
    if (replay.isPresent()) {
      Payment payment = replay.get();
      if (!order.id().equals(payment.getOrderId())) {
        throw new InvalidPaymentException("La clave de idempotencia ya se usó para otro pedido");
      }
      return new RegisteredPayment(payment, true);
    }

    if (order.status() != OrderStatus.DELIVERED) {
      throw new OrderNotPayableException(order.id(), order.status().name());
    }

    Optional<Payment> active = paymentRepositoryPort.findActiveByOrderId(order.id());
    if (active.isPresent()) {
      throw new OrderAlreadyPaidException(order.id(), active.get().getId());
    }

    CashShift shift = cashShiftRepositoryPort.findOpen().orElseThrow(ShiftNotOpenException::new);

    RegisterPaymentCommand.DiscountCommand discount = command.discount();
    List<PaymentTenderInput> tenders = command.tenders() != null ? command.tenders() : List.of();
    PaymentCalculation calculation =
        PaymentCalculator.calculate(
            order.subtotal(),
            discount != null ? discount.type() : null,
            discount != null ? discount.percentageBasisPoints() : 0,
            discount != null ? discount.amount() : null,
            discount != null ? discount.reason() : null,
            command.tipAmount(),
            tenders);

    LocalDateTime now = LocalDateTime.now();
    Payment payment =
        Payment.builder()
            .orderId(order.id())
            .shiftId(shift.getId())
            .status(PaymentStatus.COMPLETED)
            .subtotal(calculation.subtotal())
            .discount(calculation.discount())
            .tip(calculation.tip())
            .total(calculation.total())
            .tendered(calculation.tendered())
            .change(calculation.change())
            .tenders(toTenders(tenders))
            .cashierId(command.cashierId())
            .idempotencyKey(idempotencyKey)
            .createdAt(now)
            .completedAt(now)
            .build();

    return new RegisteredPayment(paymentRepositoryPort.save(payment), false);
  }

  private static List<PaymentTender> toTenders(List<PaymentTenderInput> inputs) {
    List<PaymentTender> tenders = new ArrayList<>(inputs.size());
    for (int i = 0; i < inputs.size(); i++) {
      PaymentTenderInput input = inputs.get(i);
      tenders.add(new PaymentTender(null, input.method(), input.amount(), input.reference(), i));
    }
    return tenders;
  }

  private static String normalizeKey(String key) {
    if (key == null || key.isBlank()) {
      throw new InvalidPaymentException("El encabezado Idempotency-Key es obligatorio");
    }
    String normalized = key.trim();
    if (normalized.length() > MAX_IDEMPOTENCY_KEY_LENGTH) {
      throw new InvalidPaymentException(
          "El Idempotency-Key no debe superar " + MAX_IDEMPOTENCY_KEY_LENGTH + " characters");
    }
    return normalized;
  }
}
