/* (C) 2026 */

package aros.services.rms.infraestructure.category.persistence.jpa;

import aros.services.rms.core.category.domain.OptionCategory;
import aros.services.rms.core.category.domain.OptionSelectionType;
import aros.services.rms.core.category.port.output.OptionCategoryRepositoryPort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
  private final EntityManager entityManager;

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
  public List<OptionCategory> findByNameContainingIgnoreCase(String name) {
    return optionCategoryRepository.findByNameContainingIgnoreCase(name).stream()
        .map(categoryMapper::toOptionCategoryDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<OptionCategory> findAll() {
    return optionCategoryRepository.findAll().stream()
        .map(categoryMapper::toOptionCategoryDomain)
        .collect(Collectors.toList());
  }

  /**
   * {@inheritDoc}
   *
   * <p>Reads {@code option_categories.selection_type} directly. The column has been part of the
   * schema since V37 (additive migration); unknown / null values default to {@code SINGLE_CHOICE}.
   */
  @Override
  public Map<Long, String> loadSelectionTypesByIds(Collection<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return Map.of();
    }

    String sql =
        "SELECT id, COALESCE(selection_type, 'SINGLE_CHOICE') AS selection_type "
            + "FROM option_categories WHERE id IN (:ids)";
    Query query = entityManager.createNativeQuery(sql).setParameter("ids", ids);

    Map<Long, String> selectionTypes = new LinkedHashMap<>();
    for (Object rawRow : query.getResultList()) {
      Object[] row = (Object[]) rawRow;
      selectionTypes.put(
          ((Number) row[0]).longValue(),
          normalizeSelectionType(row[1] == null ? null : row[1].toString()));
    }
    return Map.copyOf(selectionTypes);
  }

  private static String normalizeSelectionType(String stored) {
    if (stored == null || stored.isBlank()) {
      return OptionSelectionType.SINGLE_CHOICE.name();
    }
    try {
      return OptionSelectionType.valueOf(stored).name();
    } catch (IllegalArgumentException unknown) {
      return OptionSelectionType.SINGLE_CHOICE.name();
    }
  }

  @Override
  public boolean existsById(Long id) {
    return optionCategoryRepository.existsById(id);
  }
}
