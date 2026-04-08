/* (C) 2026 */
package aros.services.rms.infraestructure.daymenu.persistence;

import aros.services.rms.core.daymenu.domain.DayMenu;
import aros.services.rms.core.daymenu.port.output.DayMenuRepositoryPort;
import aros.services.rms.infraestructure.product.persistence.jpa.ProductRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Persistence adapter implementing DayMenuRepositoryPort using JPA. */
@Component
@RequiredArgsConstructor
public class DayMenuPersistenceAdapter implements DayMenuRepositoryPort {

  private final DayMenuJpaRepository dayMenuJpaRepository;
  private final ProductRepository productRepository;
  private final DayMenuMapper dayMenuMapper;

  @Override
  public Optional<DayMenu> findActive() {
    return dayMenuJpaRepository.findFirstBy().map(dayMenuMapper::toDomain);
  }

  @Override
  @Transactional
  public DayMenu save(DayMenu dayMenu) {
    var product =
        productRepository
            .findById(dayMenu.getProductId())
            .orElseThrow(
                () -> new IllegalArgumentException("Product not found: " + dayMenu.getProductId()));
    var entity = dayMenuMapper.toEntity(dayMenu, product);
    return dayMenuMapper.toDomain(dayMenuJpaRepository.save(entity));
  }

  @Override
  @Transactional
  public void deleteActive() {
    dayMenuJpaRepository.deleteAll();
  }
}
