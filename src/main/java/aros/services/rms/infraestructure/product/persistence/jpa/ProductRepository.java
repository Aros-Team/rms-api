package aros.services.rms.infraestructure.product.persistence.jpa;

import aros.services.rms.infraestructure.product.persistence.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}