/* (C) 2026 */

package aros.services.rms.infraestructure.purchase.persistence.jpa.adapters;

import aros.services.rms.core.purchase.domain.PurchaseOrder;
import aros.services.rms.core.purchase.port.output.PurchaseOrderRepositoryPort;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
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
  private final CurrencyProvider currencyProvider;

  /**
   * Creates a new instance.
   *
   * @param repository the JPA repository
   * @param mapper the mapper
   * @param currencyProvider the system currency provider
   */
  public PurchaseOrderPersistenceAdapter(
      PurchaseOrderJpaRepository repository,
      PurchaseOrderMapper mapper,
      CurrencyProvider currencyProvider) {
    this.repository = repository;
    this.mapper = mapper;
    this.currencyProvider = currencyProvider;
  }

  /**
   * Persists a purchase order. @Transactional ensures the session is open during cascade saves and
   * the subsequent toDomain mapping that accesses the items collection.
   */
  @Override
  @Transactional
  public PurchaseOrder save(PurchaseOrder order) {
    return mapper.toDomain(repository.save(mapper.toEntity(order)), currencyProvider.getCurrency());
  }

  /**
   * Finds a purchase order by id. readOnly=true keeps the session open so the mapper can traverse
   * the lazy items collection without a LazyInitializationException.
   */
  @Override
  @Transactional(readOnly = true)
  public Optional<PurchaseOrder> findById(Long id) {
    return repository.findById(id).map(e -> mapper.toDomain(e, currencyProvider.getCurrency()));
  }

  /** Returns all purchase orders with their items loaded within the same session. */
  @Override
  @Transactional(readOnly = true)
  public List<PurchaseOrder> findAll() {
    return repository.findAll().stream()
        .map(e -> mapper.toDomain(e, currencyProvider.getCurrency()))
        .collect(Collectors.toList());
  }

  /** Returns purchase orders matching notes or supplier name within the same read-only session. */
  @Override
  @Transactional(readOnly = true)
  public List<PurchaseOrder> findByNotesContainingIgnoreCaseOrSupplierNameContainingIgnoreCase(
      String search) {
    return repository.findByNotesOrSupplierNameContainingIgnoreCase(search).stream()
        .map(e -> mapper.toDomain(e, currencyProvider.getCurrency()))
        .collect(Collectors.toList());
  }

  /** Returns purchase orders for a supplier with items loaded within the same session. */
  @Override
  @Transactional(readOnly = true)
  public List<PurchaseOrder> findBySupplierId(Long supplierId) {
    return repository.findBySupplierId(supplierId).stream()
        .map(e -> mapper.toDomain(e, currencyProvider.getCurrency()))
        .collect(Collectors.toList());
  }

  /** Returns purchase orders in a date range with items loaded within the same session. */
  @Override
  @Transactional(readOnly = true)
  public List<PurchaseOrder> findByPurchasedAtBetween(LocalDateTime from, LocalDateTime to) {
    return repository.findByPurchasedAtBetween(from, to).stream()
        .map(e -> mapper.toDomain(e, currencyProvider.getCurrency()))
        .collect(Collectors.toList());
  }
}
