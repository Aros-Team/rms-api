/* (C) 2026 */
package aros.services.rms.core.table.application.usecases;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.table.application.exception.InvalidTableStatusException;
import aros.services.rms.core.table.application.exception.TableNotFoundException;
import aros.services.rms.core.table.domain.Table;
import aros.services.rms.core.table.domain.TableStatus;
import aros.services.rms.core.table.port.input.TableUseCase;
import aros.services.rms.core.table.port.output.TableRepositoryPort;
import java.util.List;

/**
 * Implementation of table management use cases. Handles CRUD and status lifecycle transitions
 * between AVAILABLE, OCCUPIED, and RESERVED.
 */
public class TableUseCaseImpl implements TableUseCase {

  private final TableRepositoryPort tableRepositoryPort;
  private final Logger logger;

  public TableUseCaseImpl(TableRepositoryPort tableRepositoryPort, Logger logger) {
    this.tableRepositoryPort = tableRepositoryPort;
    this.logger = logger;
  }

  @Override
  public Table create(Table table) {
    table.setStatus(TableStatus.AVAILABLE);
    Table saved = tableRepositoryPort.save(table);
    logger.info("Table created: id={}, tableNumber={}", saved.getId(), saved.getTableNumber());
    return saved;
  }

  @Override
  public Table update(Long id, Table table) {
    Table existing =
        tableRepositoryPort.findById(id).orElseThrow(() -> new TableNotFoundException(id));

    existing.setTableNumber(table.getTableNumber());
    existing.setCapacity(table.getCapacity());

    Table saved = tableRepositoryPort.save(existing);
    logger.info("Table updated: id={}, tableNumber={}", saved.getId(), saved.getTableNumber());
    return saved;
  }

  @Override
  public List<Table> findAll() {
    return tableRepositoryPort.findAll();
  }

  @Override
  public Table findById(Long id) {
    return tableRepositoryPort.findById(id).orElseThrow(() -> new TableNotFoundException(id));
  }

  /** {@inheritDoc} Changes the table status exclusively between AVAILABLE, OCCUPIED, RESERVED. */
  @Override
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
}