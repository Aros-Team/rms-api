/* (C) 2026 */

package aros.services.rms.infraestructure.inventory.persistence.jpa.adapters;

import aros.services.rms.core.inventory.domain.SupplyVariant;
import aros.services.rms.core.inventory.port.output.SupplyVariantRepositoryPort;
import aros.services.rms.infraestructure.inventory.persistence.jpa.SupplyVariantMapper;
import aros.services.rms.infraestructure.inventory.persistence.jpa.SupplyVariantRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Adapter that connects SupplyVariantRepositoryPort with JPA repository. */
@Component
@RequiredArgsConstructor
public class SupplyVariantPersistenceAdapter implements SupplyVariantRepositoryPort {

  private final SupplyVariantRepository supplyVariantRepository;
  private final SupplyVariantMapper supplyVariantMapper;

  @Override
  public boolean existsById(Long id) {
    return supplyVariantRepository.existsById(id);
  }

  @Override
  public Optional<SupplyVariant> findById(Long id) {
    return supplyVariantRepository.findById(id).map(supplyVariantMapper::toDomain);
  }
}
