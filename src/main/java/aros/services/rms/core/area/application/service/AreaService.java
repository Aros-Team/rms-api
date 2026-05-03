/* (C) 2026 */

package aros.services.rms.core.area.application.service;

import aros.services.rms.core.area.application.exception.AreaAlreadyExistsException;
import aros.services.rms.core.area.application.exception.AreaNotFoundException;
import aros.services.rms.core.area.domain.Area;
import aros.services.rms.core.area.port.input.AreaUseCase;
import aros.services.rms.core.area.port.output.AreaRepositoryPort;
import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.infraestructure.common.exception.ServiceUnavailableException;
import java.util.List;
import java.util.Optional;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

/**
 * Implementation of area management use cases. Handles CRUD operations and enable/disable logic for
 * preparation areas.
 */
public class AreaService implements AreaUseCase {

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(AreaService.class);
  private final AreaRepositoryPort areaRepositoryPort;
  private final Logger logger;

  /**
   * Creates a new AreaService instance.
   *
   * @param areaRepositoryPort the area repository port
   * @param logger the logger instance
   */
  public AreaService(AreaRepositoryPort areaRepositoryPort, Logger logger) {
    this.areaRepositoryPort = areaRepositoryPort;
    this.logger = logger;
  }

  /** {@inheritDoc} Validates that no area with the same name exists before creating. */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public Area create(Area area) {
    Optional<Area> existing = areaRepositoryPort.findByName(area.getName());
    if (existing.isPresent()) {
      throw new AreaAlreadyExistsException(area.getName());
    }

    area.setEnabled(true);
    Area saved = areaRepositoryPort.save(area);
    logger.info("Area created: id={}, name={}", saved.getId(), saved.getName());
    return saved;
  }

  /**
   * Recovery handler for create operation when database is unavailable.
   *
   * @param e the data access exception
   * @param area the area that was being created
   * @return never returns, always throws ServiceUnavailableException
   * @throws ServiceUnavailableException when database is unavailable
   */
  @Recover
  public Area recoverCreate(DataAccessException e, Area area) {
    log.warn(
        "BD no disponible - fallback para create(area={}): {}", area.getName(), e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  /** {@inheritDoc} Updates name and type of an existing area. */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public Area update(Long id, Area area) {
    Area existing =
        areaRepositoryPort.findById(id).orElseThrow(() -> new AreaNotFoundException(id));

    existing.setName(area.getName());
    existing.setType(area.getType());

    Area saved = areaRepositoryPort.save(existing);
    logger.info("Area updated: id={}, name={}", saved.getId(), saved.getName());
    return saved;
  }

  /**
   * Recovery handler for update operation when database is unavailable.
   *
   * @param e the data access exception
   * @param id the area identifier being updated
   * @param area the area data with updates
   * @return never returns, always throws ServiceUnavailableException
   * @throws ServiceUnavailableException when database is unavailable
   */
  @Recover
  public Area recoverUpdate(DataAccessException e, Long id, Area area) {
    log.warn("BD no disponible - fallback para update(id={}): {}", id, e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  /** {@inheritDoc} */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public List<Area> findAll() {
    return areaRepositoryPort.findAll();
  }

  /**
   * Recovery handler for findAll operation when database is unavailable.
   *
   * @param e the data access exception
   * @return never returns, always throws ServiceUnavailableException
   * @throws ServiceUnavailableException when database is unavailable
   */
  @Recover
  public List<Area> recoverFindAll(DataAccessException e) {
    log.warn("BD no disponible - fallback para findAll: {}", e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  /** {@inheritDoc} */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public Area findById(Long id) {
    return areaRepositoryPort.findById(id).orElseThrow(() -> new AreaNotFoundException(id));
  }

  /**
   * Recovery handler for findById operation when database is unavailable.
   *
   * @param e the data access exception
   * @param id the area identifier being looked up
   * @return never returns, always throws ServiceUnavailableException
   * @throws ServiceUnavailableException when database is unavailable
   */
  @Recover
  public Area recoverFindById(DataAccessException e, Long id) {
    log.warn("BD no disponible - fallback para findById(id={}): {}", id, e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  /** {@inheritDoc} Toggles the enabled flag to control whether the area receives new products. */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public Area toggleEnabled(Long id) {
    Area existing =
        areaRepositoryPort.findById(id).orElseThrow(() -> new AreaNotFoundException(id));

    existing.setEnabled(!existing.isEnabled());

    Area saved = areaRepositoryPort.save(existing);
    logger.info("Area toggled: id={}, enabled={}", saved.getId(), saved.isEnabled());
    return saved;
  }

  /**
   * Recovery handler for toggleEnabled operation when database is unavailable.
   *
   * @param e the data access exception
   * @param id the area identifier being toggled
   * @return never returns, always throws ServiceUnavailableException
   * @throws ServiceUnavailableException when database is unavailable
   */
  @Recover
  public Area recoverToggleEnabled(DataAccessException e, Long id) {
    log.warn("BD no disponible - fallback para toggleEnabled(id={}): {}", id, e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }
}
