/* (C) 2026 */
package aros.services.rms.core.table.application.usecases;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.table.application.exception.InvalidTableStatusException;
import aros.services.rms.core.table.application.exception.TableNotFoundException;
import aros.services.rms.core.table.domain.Table;
import aros.services.rms.core.table.domain.TableStatus;
import aros.services.rms.core.table.port.input.TableUseCase;
import aros.services.rms.core.table.port.output.TableRepositoryPort;
import aros.services.rms.infraestructure.common.exception.ServiceUnavailableException;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

/**
 * Implementation of table management use cases. Handles CRUD and status lifecycle transitions
 * between AVAILABLE, OCCUPIED, and RESERVED.
 */
public class TableUseCaseImpl implements TableUseCase {

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(TableUseCaseImpl.class);
  private final TableRepositoryPort tableRepositoryPort;
  private final Logger logger;

  public TableUseCaseImpl(TableRepositoryPort tableRepositoryPort, Logger logger) {
    this.tableRepositoryPort = tableRepositoryPort;
    this.logger = logger;
  }

  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public Table create(Table table) {
    table.setStatus(TableStatus.AVAILABLE);
    Table saved = tableRepositoryPort.save(table);
    logger.info("Table created: id={}, tableNumber={}", saved.getId(), saved.getTableNumber());
    return saved;
  }

  @Recover
  public Table recoverCreate(DataAccessException e, Table table) {
    log.warn(
        "BD no disponible - fallback para create(tableNumber={}): {}",
        table.getTableNumber(),
        e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public Table update(Long id, Table table) {
    Table existing =
        tableRepositoryPort.findById(id).orElseThrow(() -> new TableNotFoundException(id));

    existing.setTableNumber(table.getTableNumber());
    existing.setCapacity(table.getCapacity());

    Table saved = tableRepositoryPort.save(existing);
    logger.info("Table updated: id={}, tableNumber={}", saved.getId(), saved.getTableNumber());
    return saved;
  }

  @Recover
  public Table recoverUpdate(DataAccessException e, Long id, Table table) {
    log.warn("BD no disponible - fallback para update(id={}): {}", id, e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public List<Table> findAll() {
    return tableRepositoryPort.findAll();
  }

  @Recover
  public List<Table> recoverFindAll(DataAccessException e) {
    log.warn("BD no disponible - fallback para findAll: {}", e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public Table findById(Long id) {
    return tableRepositoryPort.findById(id).orElseThrow(() -> new TableNotFoundException(id));
  }

  @Recover
  public Table recoverFindById(DataAccessException e, Long id) {
    log.warn("BD no disponible - fallback para findById(id={}): {}", id, e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  /** {@inheritDoc} Changes the table status exclusively between AVAILABLE, OCCUPIED, RESERVED. */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public Table changeStatus(Long id, TableStatus status) {
    Table existing =
        tableRepositoryPort.findById(id).orElseThrow(() -> new TableNotFoundException(id));

    if (status == null) {
      throw new InvalidTableStatusException("Status cannot be null");
    }

    existing.setStatus(status);

    Table saved = tableRepositoryPort.save(existing);
    logger.info("Table status changed: id={}, status={}", saved.getId(), saved.getStatus());
    return saved;
  }

  @Recover
  public Table recoverChangeStatus(DataAccessException e, Long id, TableStatus status) {
    log.warn(
        "BD no disponible - fallback para changeStatus(id={}, status={}): {}",
        id,
        status,
        e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }
}
