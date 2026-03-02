package aros.services.rms.infraestructure.order.persistence.jpa;

import aros.services.rms.infraestructure.order.persistence.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}