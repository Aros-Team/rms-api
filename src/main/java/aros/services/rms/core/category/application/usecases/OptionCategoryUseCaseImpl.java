/* (C) 2026 */
package aros.services.rms.core.category.application.usecases;

import aros.services.rms.core.category.application.exception.OptionCategoryNotFoundException;
import aros.services.rms.core.category.domain.OptionCategory;
import aros.services.rms.core.category.port.input.OptionCategoryUseCase;
import aros.services.rms.core.category.port.output.OptionCategoryRepositoryPort;
import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.infraestructure.common.exception.ServiceUnavailableException;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

/**
 * Implementation of option category management use cases. Handles CRUD for customization categories
 * (e.g., "Cooking term", "Milk type").
 */
public class OptionCategoryUseCaseImpl implements OptionCategoryUseCase {

  private static final org.slf4j.Logger log =
      LoggerFactory.getLogger(OptionCategoryUseCaseImpl.class);
  private final OptionCategoryRepositoryPort optionCategoryRepositoryPort;
  private final Logger logger;

  public OptionCategoryUseCaseImpl(
      OptionCategoryRepositoryPort optionCategoryRepositoryPort, Logger logger) {
    this.optionCategoryRepositoryPort = optionCategoryRepositoryPort;
    this.logger = logger;
  }

  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public OptionCategory create(OptionCategory optionCategory) {
    OptionCategory saved = optionCategoryRepositoryPort.save(optionCategory);
    logger.info("OptionCategory created: id={}, name={}", saved.getId(), saved.getName());
    return saved;
  }

  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public OptionCategory update(Long id, OptionCategory optionCategory) {
    OptionCategory existing =
        optionCategoryRepositoryPort
            .findById(id)
            .orElseThrow(() -> new OptionCategoryNotFoundException(id));

    existing.setName(optionCategory.getName());
    existing.setDescription(optionCategory.getDescription());

    OptionCategory saved = optionCategoryRepositoryPort.save(existing);
    logger.info("OptionCategory updated: id={}, name={}", saved.getId(), saved.getName());
    return saved;
  }

  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public List<OptionCategory> findAll() {
    return optionCategoryRepositoryPort.findAll();
  }

  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public OptionCategory findById(Long id) {
    return optionCategoryRepositoryPort
        .findById(id)
        .orElseThrow(() -> new OptionCategoryNotFoundException(id));
  }

  @Recover
  public OptionCategory recoverCreate(DataAccessException e, OptionCategory optionCategory) {
    log.warn(
        "BD no disponible - fallback para create(optionCategory={}): {}",
        optionCategory.getName(),
        e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  @Recover
  public OptionCategory recoverUpdate(
      DataAccessException e, Long id, OptionCategory optionCategory) {
    log.warn("BD no disponible - fallback para update(id={}): {}", id, e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  @Recover
  public List<OptionCategory> recoverFindAll(DataAccessException e) {
    log.warn("BD no disponible - fallback para findAll: {}", e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  @Recover
  public OptionCategory recoverFindById(DataAccessException e, Long id) {
    log.warn("BD no disponible - fallback para findById(id={}): {}", id, e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }
}
