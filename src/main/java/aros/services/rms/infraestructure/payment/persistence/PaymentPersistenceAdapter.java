/* (C) 2026 */

package aros.services.rms.infraestructure.payment.persistence;

import aros.services.rms.core.payment.application.dto.PaymentFilter;
import aros.services.rms.core.payment.application.exception.PaymentNotFoundException;
import aros.services.rms.core.payment.domain.Payment;
import aros.services.rms.core.payment.domain.PaymentQueryResult;
import aros.services.rms.core.payment.domain.PaymentStatus;
import aros.services.rms.core.payment.port.output.PaymentRepositoryPort;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

/** Adapter for payment persistence. */
@Repository
@RequiredArgsConstructor
@Transactional
public class PaymentPersistenceAdapter implements PaymentRepositoryPort {

  private static final String RECEIPT_PREFIX = "R-";
  private static final DateTimeFormatter RECEIPT_DATE = DateTimeFormatter.BASIC_ISO_DATE;

  private final PaymentJpaRepository paymentRepository;
  private final PaymentEntityMapper mapper;

  /** {@inheritDoc} */
  @Override
  public Payment save(Payment payment) {
    if (payment.getId() == null) {
      PaymentEntity saved = paymentRepository.save(mapper.toEntity(payment));
      if (saved.getReceiptNumber() == null) {
        saved.setReceiptNumber(receiptNumber(saved));
        saved = paymentRepository.save(saved);
      }
      return mapper.toDomain(saved);
    }

    PaymentEntity existing =
        paymentRepository
            .findWithTendersById(payment.getId())
            .orElseThrow(() -> new PaymentNotFoundException(payment.getId()));
    existing.setStatus(payment.getStatus());
    existing.setVoidedAt(payment.getVoidedAt());
    existing.setVoidedBy(payment.getVoidedBy());
    existing.setVoidReason(payment.getVoidReason());
    if (existing.getReceiptNumber() == null) {
      existing.setReceiptNumber(receiptNumber(existing));
    }
    return mapper.toDomain(paymentRepository.save(existing));
  }

  /** {@inheritDoc} */
  @Override
  public Optional<Payment> findById(Long id) {
    return paymentRepository.findWithTendersById(id).map(mapper::toDomain);
  }

  /** {@inheritDoc} */
  @Override
  public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
    return paymentRepository.findByIdempotencyKey(idempotencyKey).map(mapper::toDomain);
  }

  /** {@inheritDoc} */
  @Override
  public Optional<Payment> findActiveByOrderId(Long orderId) {
    return paymentRepository
        .findFirstByOrderIdAndStatusNot(orderId, PaymentStatus.VOIDED)
        .map(mapper::toDomain);
  }

  /** {@inheritDoc} */
  @Override
  public List<Payment> findByShiftId(Long shiftId) {
    return paymentRepository.findByShiftId(shiftId).stream().map(mapper::toDomain).toList();
  }

  /** {@inheritDoc} */
  @Override
  public PaymentQueryResult search(PaymentFilter filter) {
    int page = Math.max(filter.page(), 0);
    int size = clampSize(filter.size());
    LocalDate date = filter.date();
    var start = date != null ? date.atStartOfDay() : null;
    var end = date != null ? date.plusDays(1).atStartOfDay() : null;

    Page<PaymentEntity> result =
        paymentRepository.search(
            start,
            end,
            filter.status(),
            filter.method(),
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "completedAt")));

    List<Payment> items = result.getContent().stream().map(mapper::toDomain).toList();
    return new PaymentQueryResult(items, result.getTotalElements(), page, size);
  }

  private static int clampSize(int size) {
    if (size <= 0) {
      return 20;
    }
    return Math.min(size, 100);
  }

  private static String receiptNumber(PaymentEntity entity) {
    return RECEIPT_PREFIX
        + RECEIPT_DATE.format(entity.getCreatedAt().toLocalDate())
        + "-"
        + String.format("%06d", entity.getId());
  }
}
