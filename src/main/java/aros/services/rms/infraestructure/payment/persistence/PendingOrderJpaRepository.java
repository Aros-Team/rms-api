/* (C) 2026 */

package aros.services.rms.infraestructure.payment.persistence;

import aros.services.rms.core.payment.domain.PaymentStatus;
import aros.services.rms.infraestructure.order.persistence.Order;
import aros.services.rms.infraestructure.order.persistence.OrderStatus;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Read repository for the cashier pending-orders board. */
@Repository
public interface PendingOrderJpaRepository extends JpaRepository<Order, Long> {

  /**
   * Delivered orders of a business date that have no non-voided payment.
   *
   * @param status the delivered status
   * @param start inclusive start of the business date
   * @param end exclusive end of the business date
   * @param voidedStatus the voided payment status
   * @param searchId exact order id, or null
   * @param searchNumber exact table number, or null
   * @param pageable pagination
   * @return the page of unpaid delivered orders
   */
  @Query(
      value =
          "SELECT o FROM Order o LEFT JOIN o.table t "
              + "WHERE o.status = :status "
              + "AND o.date >= :start AND o.date < :end "
              + "AND NOT EXISTS (SELECT p.id FROM PaymentEntity p "
              + "  WHERE p.orderId = o.id AND p.status <> :voidedStatus) "
              + "AND (:searchId IS NULL OR o.id = :searchId OR t.tableNumber = :searchNumber)",
      countQuery =
          "SELECT COUNT(o) FROM Order o LEFT JOIN o.table t "
              + "WHERE o.status = :status "
              + "AND o.date >= :start AND o.date < :end "
              + "AND NOT EXISTS (SELECT p.id FROM PaymentEntity p "
              + "  WHERE p.orderId = o.id AND p.status <> :voidedStatus) "
              + "AND (:searchId IS NULL OR o.id = :searchId OR t.tableNumber = :searchNumber)")
  Page<Order> findPending(
      @Param("status") OrderStatus status,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end,
      @Param("voidedStatus") PaymentStatus voidedStatus,
      @Param("searchId") Long searchId,
      @Param("searchNumber") Integer searchNumber,
      Pageable pageable);
}
