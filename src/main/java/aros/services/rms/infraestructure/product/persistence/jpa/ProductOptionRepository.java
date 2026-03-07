package aros.services.rms.infraestructure.product.persistence.jpa;

import aros.services.rms.infraestructure.product.persistence.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {
}