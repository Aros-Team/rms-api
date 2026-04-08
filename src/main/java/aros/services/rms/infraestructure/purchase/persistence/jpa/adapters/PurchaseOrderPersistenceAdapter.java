/* (C) 2026 */
package aros.services.rms.infraestructure.purchase.persistence.jpa.adapters;

import aros.services.rms.core.purchase.domain.PurchaseOrder;
import aros.services.rms.core.purchase.port.output.PurchaseOrderRepositoryPort;
import aros.services.rms.infraestructure.purchase.persistence.jpa.PurchaseOrderJpaRepository;
import aros.services.rms.infraestructure.purchase.persistence.jpa.PurchaseOrderMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Adapter that implements PurchaseOrderRepositoryPort using JPA. */
@Component
public class PurchaseOrderPersistenceAdapter implements PurchaseOrderRepositoryPort {

  private final PurchaseOrderJpaRepository repository;
  private final PurchaseOrderMapper mapper;

  public PurchaseOrderPersistenceAdapter(
      PurchaseOrderJpaRepository repository, PurchaseOrderMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  /**
   * Persists a purchase order. @Transactional ensures the session is open during cascade saves and
   * the subsequent toDomain mapping that accesses the items collection.
   */
  @Override
  @Transactional
  public PurchaseOrder save(PurchaseOrder order) {
    return mapper.toDomain(repository.save(mapper.toEntity(order)));
  }

  /**
   * Finds a purchase order by id. readOnly=true keeps the session open so the mapper can traverse
   * the lazy items collection without a LazyInitializationException.
   */
  @Override
  @Transactional(readOnly = true)
  public Optional<PurchaseOrder> findById(Long id) {
    return repository.findById(id).map(mapper::toDomain);
  }

  /** Returns all purchase orders with their items loaded within the same session. */
  @Override
  @Transactional(readOnly = true)
  public List<PurchaseOrder> findAll() {
    return repository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
  }

  /** Returns purchase orders for a supplier with items loaded within the same session. */
  @Override
  @Transactional(readOnly = true)
  public List<PurchaseOrder> findBySupplierId(Long supplierId) {
    return repository.findBySupplierId(supplierId).stream()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
  }

  /** Returns purchase orders in a date range with items loaded within the same session. */
  @Override
  @Transactional(readOnly = true)
  public List<PurchaseOrder> findByPurchasedAtBetween(LocalDateTime from, LocalDateTime to) {
    return repository.findByPurchasedAtBetween(from, to).stream()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
  }
}
