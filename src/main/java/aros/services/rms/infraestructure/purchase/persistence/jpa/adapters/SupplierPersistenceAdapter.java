/* (C) 2026 */
package aros.services.rms.infraestructure.purchase.persistence.jpa.adapters;

import aros.services.rms.core.purchase.domain.Supplier;
import aros.services.rms.core.purchase.port.output.SupplierRepositoryPort;
import aros.services.rms.infraestructure.purchase.persistence.jpa.SupplierJpaRepository;
import aros.services.rms.infraestructure.purchase.persistence.jpa.SupplierMapper;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Adapter that implements SupplierRepositoryPort using JPA. */
@Component
public class SupplierPersistenceAdapter implements SupplierRepositoryPort {

  private final SupplierJpaRepository repository;
  private final SupplierMapper mapper;

  public SupplierPersistenceAdapter(SupplierJpaRepository repository, SupplierMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public Supplier save(Supplier supplier) {
    return mapper.toDomain(repository.save(mapper.toEntity(supplier)));
  }

  @Override
  public Optional<Supplier> findById(Long id) {
    return repository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<Supplier> findAll() {
    return repository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
  }
}
