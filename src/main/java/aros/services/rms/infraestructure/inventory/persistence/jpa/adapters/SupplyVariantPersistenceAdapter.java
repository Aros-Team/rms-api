/* (C) 2026 */

package aros.services.rms.infraestructure.inventory.persistence.jpa.adapters;

import aros.services.rms.core.inventory.domain.SupplyVariant;
import aros.services.rms.core.inventory.port.output.SupplyVariantRepositoryPort;
import aros.services.rms.infraestructure.inventory.persistence.jpa.SupplyVariantMapper;
import aros.services.rms.infraestructure.inventory.persistence.jpa.SupplyVariantRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

  @Override
  public List<SupplyVariant> findAllById(List<Long> ids) {
    return supplyVariantRepository.findAllById(ids).stream()
        .map(supplyVariantMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public Page<SupplyVariant> findByNameContainingIgnoreCase(String name, Pageable pageable) {
    return supplyVariantRepository
        .findByNameContainingIgnoreCase(name, pageable)
        .map(supplyVariantMapper::toDomain);
  }
}
