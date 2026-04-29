/* (C) 2026 */

package aros.services.rms.infraestructure.category.persistence.jpa;

import aros.services.rms.core.category.domain.OptionCategory;
import aros.services.rms.core.category.port.output.OptionCategoryRepositoryPort;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Persistence adapter that implements OptionCategoryRepositoryPort using JPA. */
@Component
@RequiredArgsConstructor
public class OptionCategoryPersistenceAdapter implements OptionCategoryRepositoryPort {

  private final OptionCategoryRepository optionCategoryRepository;
  private final CategoryMapper categoryMapper;

  @Override
  public OptionCategory save(OptionCategory optionCategory) {
    aros.services.rms.infraestructure.category.persistence.OptionCategory entity =
        categoryMapper.toOptionCategoryEntity(optionCategory);
    aros.services.rms.infraestructure.category.persistence.OptionCategory savedEntity =
        optionCategoryRepository.save(entity);
    return categoryMapper.toOptionCategoryDomain(savedEntity);
  }

  @Override
  public Optional<OptionCategory> findById(Long id) {
    return optionCategoryRepository.findById(id).map(categoryMapper::toOptionCategoryDomain);
  }

  @Override
  public List<OptionCategory> findAll() {
    return optionCategoryRepository.findAll().stream()
        .map(categoryMapper::toOptionCategoryDomain)
        .collect(Collectors.toList());
  }

  @Override
  public boolean existsById(Long id) {
    return optionCategoryRepository.existsById(id);
  }
}
