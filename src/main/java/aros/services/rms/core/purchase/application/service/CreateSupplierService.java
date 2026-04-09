/* (C) 2026 */
package aros.services.rms.core.purchase.application.service;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.purchase.application.exception.SupplierNotFoundException;
import aros.services.rms.core.purchase.domain.Supplier;
import aros.services.rms.core.purchase.port.input.CreateSupplierUseCase;
import aros.services.rms.core.purchase.port.input.GetSuppliersUseCase;
import aros.services.rms.core.purchase.port.input.UpdateSupplierUseCase;
import aros.services.rms.core.purchase.port.output.SupplierRepositoryPort;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import aros.services.rms.infraestructure.common.exception.ServiceUnavailableException;

/**
 * Implementation of supplier use cases: create, update and list.
 *
 * <p>Implements three input ports in a single class since all operations share the same repository
 * dependency and are cohesive around the Supplier aggregate.
 */
public class CreateSupplierService
    implements CreateSupplierUseCase, UpdateSupplierUseCase, GetSuppliersUseCase {

  private static final org.slf4j.Logger log =
      LoggerFactory.getLogger(CreateSupplierService.class);

  private final SupplierRepositoryPort supplierRepositoryPort;
  private final Logger logger;

  public CreateSupplierService(
      SupplierRepositoryPort supplierRepositoryPort, Logger logger) {
    this.supplierRepositoryPort = supplierRepositoryPort;
    this.logger = logger;
  }

  /**
   * Creates a new supplier. New suppliers are active by default.
   *
   * @param supplier domain object with name and contact
   * @return persisted supplier with generated id
   */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public Supplier create(Supplier supplier) {
    // Ensure new suppliers start as active
    supplier.setActive(true);
    var saved = supplierRepositoryPort.save(supplier);
    logger.info("Supplier created: id={}, name={}", saved.getId(), saved.getName());
    return saved;
  }

  @Recover
  public Supplier recoverCreate(DataAccessException e, Supplier supplier) {
    log.warn("DB unavailable - fallback create(supplier={}): {}", supplier.getName(), e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  /**
   * Updates name, contact and active status of an existing supplier.
   *
   * @param id supplier identifier
   * @param supplier domain object with updated fields
   * @return updated supplier
   * @throws SupplierNotFoundException if no supplier exists with the given id
   */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public Supplier update(Long id, Supplier supplier) {
    var existing =
        supplierRepositoryPort.findById(id).orElseThrow(() -> new SupplierNotFoundException(id));

    // Apply changes from request
    existing.setName(supplier.getName());
    existing.setContact(supplier.getContact());
    existing.setActive(supplier.isActive());

    var saved = supplierRepositoryPort.save(existing);
    logger.info("Supplier updated: id={}, name={}, active={}", saved.getId(), saved.getName(), saved.isActive());
    return saved;
  }

  @Recover
  public Supplier recoverUpdate(DataAccessException e, Long id, Supplier supplier) {
    log.warn("DB unavailable - fallback update(id={}): {}", id, e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  /**
   * Returns all suppliers regardless of active status.
   *
   * @return list of all suppliers
   */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public List<Supplier> findAll() {
    return supplierRepositoryPort.findAll();
  }

  @Recover
  public List<Supplier> recoverFindAll(DataAccessException e) {
    log.warn("DB unavailable - fallback findAll: {}", e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  /**
   * Finds a supplier by its identifier.
   *
   * @param id supplier identifier
   * @return supplier domain object
   * @throws SupplierNotFoundException if not found
   */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public Supplier findById(Long id) {
    return supplierRepositoryPort.findById(id).orElseThrow(() -> new SupplierNotFoundException(id));
  }

  @Recover
  public Supplier recoverFindById(DataAccessException e, Long id) {
    log.warn("DB unavailable - fallback findById(id={}): {}", id, e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }
}
