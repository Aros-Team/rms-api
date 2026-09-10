/* (C) 2026 */

package aros.services.rms.infraestructure.inventory.persistence.jpa.adapters;

import aros.services.rms.core.inventory.domain.SupplyVariant;
import aros.services.rms.core.inventory.port.output.SupplyVariantRepositoryPort;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
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
  private final CurrencyProvider currencyProvider;

  @Override
  public boolean existsById(Long id) {
    return supplyVariantRepository.existsById(id);
  }

  @Override
  public Optional<SupplyVariant> findById(Long id) {
    return supplyVariantRepository
        .findById(id)
        .map(e -> supplyVariantMapper.toDomain(e, currencyProvider.getCurrency()));
  }

  @Override
  public List<SupplyVariant> findAllById(List<Long> ids) {
    return supplyVariantRepository.findAllById(ids).stream()
        .map(e -> supplyVariantMapper.toDomain(e, currencyProvider.getCurrency()))
        .collect(Collectors.toList());
  }

  @Override
  public Page<SupplyVariant> findByNameContainingIgnoreCase(String name, Pageable pageable) {
    return supplyVariantRepository
        .findByNameContainingIgnoreCase(name, pageable)
        .map(e -> supplyVariantMapper.toDomain(e, currencyProvider.getCurrency()));
  }
}
