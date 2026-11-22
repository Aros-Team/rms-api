/* (C) 2026 */

package aros.services.rms.infraestructure.payment.persistence;

import aros.services.rms.core.payment.domain.PaymentStatus;
import aros.services.rms.core.payment.domain.PaymentTenderMethod;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Spring Data repository for payments. */
@Repository
public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {

  /**
   * Finds a payment with its tenders.
   *
   * @param id the payment id
   * @return the payment when present
   */
  @EntityGraph(attributePaths = "tenders")
  @Query("SELECT p FROM PaymentEntity p WHERE p.id = :id")
  Optional<PaymentEntity> findWithTendersById(@Param("id") Long id);

  /**
   * Finds a payment by idempotency key.
   *
   * @param idempotencyKey the key
   * @return the original payment when present
   */
  Optional<PaymentEntity> findByIdempotencyKey(String idempotencyKey);

  /**
   * Finds the non-voided payment of an order.
   *
   * @param orderId the order id
   * @param voided the voided status to exclude
   * @return the active payment when present
   */
  Optional<PaymentEntity> findFirstByOrderIdAndStatusNot(Long orderId, PaymentStatus voided);

  /**
   * Returns every payment of a shift with tenders.
   *
   * @param shiftId the shift id
   * @return the payments
   */
  @EntityGraph(attributePaths = "tenders")
  List<PaymentEntity> findByShiftId(Long shiftId);

  /**
   * Returns the order ids that have a payment in the given status.
   *
   * @param orderIds the order ids to inspect
   * @param status the payment status
   * @return matching order ids
   */
  @Query(
      "SELECT DISTINCT p.orderId FROM PaymentEntity p "
          + "WHERE p.orderId IN :orderIds AND p.status = :status")
  List<Long> findOrderIdsByStatus(
      @Param("orderIds") List<Long> orderIds, @Param("status") PaymentStatus status);

  /**
   * Paged payment journal with optional date, status and tender-method filters.
   *
   * @param start inclusive lower bound for {@code completedAt}, or null
   * @param end exclusive upper bound for {@code completedAt}, or null
   * @param status payment status filter, or null
   * @param method tender method filter, or null
   * @param pageable pagination
   * @return the page
   */
  @Query(
      "SELECT p FROM PaymentEntity p "
          + "WHERE (:start IS NULL OR p.completedAt >= :start) "
          + "AND (:end IS NULL OR p.completedAt < :end) "
          + "AND (:status IS NULL OR p.status = :status) "
          + "AND (:method IS NULL OR EXISTS ("
          + "  SELECT t.id FROM PaymentTenderEntity t "
          + "  WHERE t.payment = p AND t.method = :method))")
  Page<PaymentEntity> search(
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end,
      @Param("status") PaymentStatus status,
      @Param("method") PaymentTenderMethod method,
      Pageable pageable);
}
