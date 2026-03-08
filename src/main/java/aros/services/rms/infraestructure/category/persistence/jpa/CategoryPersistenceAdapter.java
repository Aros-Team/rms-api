/* (C) 2026 */
package aros.services.rms.infraestructure.category.persistence.jpa;

import aros.services.rms.core.category.domain.Category;
import aros.services.rms.core.category.port.output.CategoryRepositoryPort;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Persistence adapter that implements CategoryRepositoryPort using JPA. */
@Component
@RequiredArgsConstructor
public class CategoryPersistenceAdapter implements CategoryRepositoryPort {

  private final CategoryRepository categoryRepository;
  private final CategoryMapper categoryMapper;

  @Override
  public Category save(Category category) {
    aros.services.rms.infraestructure.category.persistence.Category entity =
        categoryMapper.toEntity(category);
    aros.services.rms.infraestructure.category.persistence.Category savedEntity =
        categoryRepository.save(entity);
    return categoryMapper.toDomain(savedEntity);
  }

  @Override
  public Optional<Category> findById(Long id) {
    return categoryRepository.findById(id).map(categoryMapper::toDomain);
  }

  @Override
  public Optional<Category> findByName(String name) {
    return categoryRepository.findByName(name).map(categoryMapper::toDomain);
  }

  @Override
  public List<Category> findAll() {
    return categoryRepository.findAll().stream()
        .map(categoryMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public boolean existsById(Long id) {
    return categoryRepository.existsById(id);
  }
}