/* (C) 2026 */

package aros.services.rms.infraestructure.payment.persistence;

import aros.services.rms.infraestructure.order.persistence.Order;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Payment-owned repository that loads an order with a pessimistic write lock. */
@Repository
public interface PayableOrderJpaRepository extends JpaRepository<Order, Long> {

  /**
   * Loads an order for update. Details are loaded lazily inside the charge transaction.
   *
   * @param id the order id
   * @return the locked order when present
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT o FROM Order o WHERE o.id = :id")
  Optional<Order> findByIdForUpdate(@Param("id") Long id);
}
