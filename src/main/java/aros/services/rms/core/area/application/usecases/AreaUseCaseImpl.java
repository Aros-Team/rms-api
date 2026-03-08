/* (C) 2026 */
package aros.services.rms.core.area.application.usecases;

import aros.services.rms.core.area.application.exception.AreaAlreadyExistsException;
import aros.services.rms.core.area.application.exception.AreaNotFoundException;
import aros.services.rms.core.area.domain.Area;
import aros.services.rms.core.area.port.input.AreaUseCase;
import aros.services.rms.core.area.port.output.AreaRepositoryPort;
import aros.services.rms.core.common.logger.Logger;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of area management use cases. Handles CRUD operations and enable/disable logic for
 * preparation areas.
 */
public class AreaUseCaseImpl implements AreaUseCase {

  private final AreaRepositoryPort areaRepositoryPort;
  private final Logger logger;

  public AreaUseCaseImpl(AreaRepositoryPort areaRepositoryPort, Logger logger) {
    this.areaRepositoryPort = areaRepositoryPort;
    this.logger = logger;
  }

  /** {@inheritDoc} Validates that no area with the same name exists before creating. */
  @Override
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

  /** {@inheritDoc} Updates name and type of an existing area. */
  @Override
  public Area update(Long id, Area area) {
    Area existing =
        areaRepositoryPort.findById(id).orElseThrow(() -> new AreaNotFoundException(id));

    existing.setName(area.getName());
    existing.setType(area.getType());

    Area saved = areaRepositoryPort.save(existing);
    logger.info("Area updated: id={}, name={}", saved.getId(), saved.getName());
    return saved;
  }

  /** {@inheritDoc} */
  @Override
  public List<Area> findAll() {
    return areaRepositoryPort.findAll();
  }

  /** {@inheritDoc} */
  @Override
  public Area findById(Long id) {
    return areaRepositoryPort.findById(id).orElseThrow(() -> new AreaNotFoundException(id));
  }

  /** {@inheritDoc} Toggles the enabled flag to control whether the area receives new products. */
  @Override
  public Area toggleEnabled(Long id) {
    Area existing =
        areaRepositoryPort.findById(id).orElseThrow(() -> new AreaNotFoundException(id));

    existing.setEnabled(!existing.isEnabled());

    Area saved = areaRepositoryPort.save(existing);
    logger.info("Area toggled: id={}, enabled={}", saved.getId(), saved.isEnabled());
    return saved;
  }
}