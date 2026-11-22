/* (C) 2026 */

package aros.services.rms.infraestructure.payment.api.dto;

import aros.services.rms.core.payment.domain.Payment;
import aros.services.rms.core.payment.domain.PaymentTender;
import aros.services.rms.core.payment.domain.PendingOrder;
import aros.services.rms.infraestructure.common.dto.MoneyDto;
import java.util.List;
import org.springframework.stereotype.Component;

/** Maps payment domain objects to the wire DTOs. */
@Component
public class PaymentApiMapper {

  /**
   * Builds the full payment response.
   *
   * @param payment the domain payment
   * @return the response DTO
   */
  public PaymentResponse toResponse(Payment payment) {
    List<PaymentResponse.TenderResponse> tenders =
        payment.getTenders() == null
            ? List.of()
            : payment.getTenders().stream().map(this::toTender).toList();
    return new PaymentResponse(
        payment.getId(),
        payment.getOrderId(),
        payment.getStatus() != null ? payment.getStatus().name() : null,
        payment.getReceiptNumber(),
        payment.getSubtotal().currency().getCurrencyCode(),
        MoneyDto.of(payment.getSubtotal()),
        MoneyDto.of(payment.discountAmount()),
        MoneyDto.of(payment.getTip()),
        MoneyDto.of(payment.getTotal()),
        MoneyDto.of(payment.getTendered()),
        MoneyDto.of(payment.getChange()),
        tenders,
        payment.getCashierId(),
        payment.getShiftId(),
        payment.getCreatedAt(),
        payment.getCompletedAt(),
        payment.getVoidedAt(),
        payment.getVoidedBy(),
        payment.getVoidReason());
  }

  /**
   * Builds the compact payment row.
   *
   * @param payment the domain payment
   * @return the summary DTO
   */
  public PaymentSummaryResponse toSummary(Payment payment) {
    return new PaymentSummaryResponse(
        payment.getId(),
        payment.getOrderId(),
        payment.getStatus() != null ? payment.getStatus().name() : null,
        payment.getReceiptNumber(),
        payment.getSubtotal().currency().getCurrencyCode(),
        MoneyDto.of(payment.getSubtotal()),
        MoneyDto.of(payment.discountAmount()),
        MoneyDto.of(payment.getTip()),
        MoneyDto.of(payment.getTotal()),
        payment.getCashierId(),
        payment.getShiftId(),
        payment.getCompletedAt(),
        payment.getVoidedAt(),
        payment.getVoidReason());
  }

  /**
   * Builds a pending-order card.
   *
   * @param pending the pending order read model
   * @return the response DTO
   */
  public PendingOrderResponse toPending(PendingOrder pending) {
    return new PendingOrderResponse(
        pending.orderId(),
        pending.tableId(),
        pending.tableNumber(),
        pending.partySize(),
        pending.deliveredAt(),
        MoneyDto.of(pending.total()),
        pending.paymentStatus().name());
  }

  private PaymentResponse.TenderResponse toTender(PaymentTender tender) {
    return new PaymentResponse.TenderResponse(
        tender.method().name(), MoneyDto.of(tender.amount()), tender.reference());
  }
}
