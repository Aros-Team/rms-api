/* (C) 2026 */

package aros.services.rms.core.table.application.service;

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
public class TableService implements TableUseCase {

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(TableService.class);
  private final TableRepositoryPort tableRepositoryPort;
  private final Logger logger;

  /**
   * Creates a new table service instance.
   *
   * @param tableRepositoryPort the table repository port
   * @param logger the logger instance
   */
  public TableService(TableRepositoryPort tableRepositoryPort, Logger logger) {
    this.tableRepositoryPort = tableRepositoryPort;
    this.logger = logger;
  }

  /**
   * Creates a new table with AVAILABLE status.
   *
   * @param table the table data to create
   * @return the created table with generated ID
   */
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

  /**
   * Recovery handler for create operation when database is unavailable.
   *
   * @param e the data access exception
   * @param table the table that was being created
   * @return never returns, always throws ServiceUnavailableException
   * @throws ServiceUnavailableException when database is unavailable
   */
  @Recover
  public Table recoverCreate(DataAccessException e, Table table) {
    log.warn(
        "BD no disponible - fallback para create(tableNumber={}): {}",
        table.getTableNumber(),
        e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  /**
   * Updates an existing table.
   *
   * @param id the table identifier
   * @param table the table data with updates
   * @return the updated table
   * @throws TableNotFoundException if table does not exist
   */
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

  /**
   * Recovery handler for update operation when database is unavailable.
   *
   * @param e the data access exception
   * @param id the table identifier being updated
   * @param table the table data with updates
   * @return never returns, always throws ServiceUnavailableException
   * @throws ServiceUnavailableException when database is unavailable
   */
  @Recover
  public Table recoverUpdate(DataAccessException e, Long id, Table table) {
    log.warn("BD no disponible - fallback para update(id={}): {}", id, e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  /**
   * Retrieves all tables.
   *
   * @return list of all tables
   */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public List<Table> findAll() {
    return tableRepositoryPort.findAll();
  }

  /**
   * Recovery handler for findAll operation when database is unavailable.
   *
   * @param e the data access exception
   * @return never returns, always throws ServiceUnavailableException
   * @throws ServiceUnavailableException when database is unavailable
   */
  @Recover
  public List<Table> recoverFindAll(DataAccessException e) {
    log.warn("BD no disponible - fallback para findAll: {}", e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  /**
   * Finds a table by its identifier.
   *
   * @param id the table identifier
   * @return the found table
   * @throws TableNotFoundException if not found
   */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public Table findById(Long id) {
    return tableRepositoryPort.findById(id).orElseThrow(() -> new TableNotFoundException(id));
  }

  /**
   * Recovery handler for findById operation when database is unavailable.
   *
   * @param e the data access exception
   * @param id the table identifier being looked up
   * @return never returns, always throws ServiceUnavailableException
   * @throws ServiceUnavailableException when database is unavailable
   */
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

  /**
   * Recovery handler for changeStatus operation when database is unavailable.
   *
   * @param e the data access exception
   * @param id the table identifier
   * @param status the new status
   * @return never returns, always throws ServiceUnavailableException
   * @throws ServiceUnavailableException when database is unavailable
   */
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
