/* (C) 2026 */

package aros.services.rms.core.daymenu.application.service;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.daymenu.domain.DayMenu;
import aros.services.rms.core.daymenu.domain.DayMenuHistory;
import aros.services.rms.core.daymenu.port.input.UpdateDayMenuUseCase;
import aros.services.rms.core.daymenu.port.output.DayMenuHistoryRepositoryPort;
import aros.services.rms.core.daymenu.port.output.DayMenuRepositoryPort;
import aros.services.rms.core.product.application.exception.ProductNotFoundException;
import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.port.output.ProductRepositoryPort;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Use case implementation for updating the active day menu. Archives the previous entry and creates
 * a new one atomically (transaction managed by the infrastructure layer).
 */
public class UpdateDayMenuService implements UpdateDayMenuUseCase {

  private final ProductRepositoryPort productRepositoryPort;
  private final DayMenuRepositoryPort dayMenuRepositoryPort;
  private final DayMenuHistoryRepositoryPort dayMenuHistoryRepositoryPort;
  private final Logger logger;

  /**
   * Creates a new service instance.
   *
   * @param productRepositoryPort the product repository port
   * @param dayMenuRepositoryPort the day menu repository port
   * @param dayMenuHistoryRepositoryPort the day menu history repository port
   * @param logger the logger instance
   */
  public UpdateDayMenuService(
      ProductRepositoryPort productRepositoryPort,
      DayMenuRepositoryPort dayMenuRepositoryPort,
      DayMenuHistoryRepositoryPort dayMenuHistoryRepositoryPort,
      Logger logger) {
    this.productRepositoryPort = productRepositoryPort;
    this.dayMenuRepositoryPort = dayMenuRepositoryPort;
    this.dayMenuHistoryRepositoryPort = dayMenuHistoryRepositoryPort;
    this.logger = logger;
  }

  /**
   * Updates the active day menu with a new product.
   *
   * @param productId the product identifier to set as day menu
   * @param createdBy the user identifier who created this menu
   * @return the newly created day menu entry
   * @throws ProductNotFoundException if product does not exist or is not active
   */
  @Override
  public DayMenu update(Long productId, String createdBy) {
    Product product =
        productRepositoryPort
            .findById(productId)
            .filter(Product::isActive)
            .orElseThrow(() -> new ProductNotFoundException(productId));

    LocalDateTime now = LocalDateTime.now();

    Optional<DayMenu> existing = dayMenuRepositoryPort.findActive();
    if (existing.isPresent()) {
      DayMenu current = existing.get();
      DayMenuHistory history =
          DayMenuHistory.builder()
              .productId(current.getProductId())
              .productName(current.getProductName())
              .productBasePrice(current.getProductBasePrice())
              .validFrom(current.getValidFrom())
              .validUntil(now)
              .createdBy(current.getCreatedBy())
              .build();
      dayMenuHistoryRepositoryPort.save(history);
      dayMenuRepositoryPort.deleteActive();
    }

    DayMenu newDayMenu =
        DayMenu.builder()
            .productId(product.getId())
            .productName(product.getName())
            .productBasePrice(product.getBasePrice())
            .validFrom(now)
            .createdBy(createdBy)
            .build();

    DayMenu saved = dayMenuRepositoryPort.save(newDayMenu);
    logger.info(
        "Day menu updated: productId={}, createdBy={}", saved.getProductId(), saved.getCreatedBy());
    return saved;
  }
}
