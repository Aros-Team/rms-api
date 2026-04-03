/* (C) 2026 */
package aros.services.rms.infraestructure.daymenu.persistence;

import aros.services.rms.core.daymenu.domain.DayMenuHistory;
import aros.services.rms.core.daymenu.port.output.DayMenuHistoryRepositoryPort;
import aros.services.rms.infraestructure.product.persistence.jpa.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Persistence adapter implementing DayMenuHistoryRepositoryPort using JPA. */
@Component
@RequiredArgsConstructor
public class DayMenuHistoryPersistenceAdapter implements DayMenuHistoryRepositoryPort {

  private final DayMenuHistoryJpaRepository dayMenuHistoryJpaRepository;
  private final ProductRepository productRepository;
  private final DayMenuMapper dayMenuMapper;

  @Override
  @Transactional
  public DayMenuHistory save(DayMenuHistory history) {
    var product =
        productRepository
            .findById(history.getProductId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Product not found: " + history.getProductId()));
    var entity = dayMenuMapper.toHistoryEntity(history, product);
    return dayMenuMapper.toHistoryDomain(dayMenuHistoryJpaRepository.save(entity));
  }

  @Override
  public Page<DayMenuHistory> findAll(Pageable pageable) {
    return dayMenuHistoryJpaRepository
        .findAllByOrderByValidUntilDesc(pageable)
        .map(dayMenuMapper::toHistoryDomain);
  }
}
