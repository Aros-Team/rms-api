/* (C) 2026 */

package aros.services.rms.infraestructure.category.persistence.jpa;

import aros.services.rms.core.category.domain.OptionGroup;
import aros.services.rms.core.category.domain.OptionSelectionType;
import aros.services.rms.core.category.port.output.OptionGroupRepositoryPort;
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

/** Persistence adapter that implements OptionGroupRepositoryPort using JPA. */
@Component
@RequiredArgsConstructor
public class OptionGroupPersistenceAdapter implements OptionGroupRepositoryPort {

  private final OptionGroupRepository optionGroupRepository;
  private final CategoryMapper categoryMapper;
  private final EntityManager entityManager;

  @Override
  public OptionGroup save(OptionGroup optionGroup) {
    aros.services.rms.infraestructure.category.persistence.OptionGroup entity =
        categoryMapper.toOptionGroupEntity(optionGroup);
    aros.services.rms.infraestructure.category.persistence.OptionGroup savedEntity =
        optionGroupRepository.save(entity);
    return categoryMapper.toOptionGroupDomain(savedEntity);
  }

  @Override
  public Optional<OptionGroup> findById(Long id) {
    return optionGroupRepository.findById(id).map(categoryMapper::toOptionGroupDomain);
  }

  @Override
  public List<OptionGroup> findByNameContainingIgnoreCase(String name) {
    return optionGroupRepository.findByNameContainingIgnoreCase(name).stream()
        .map(categoryMapper::toOptionGroupDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<OptionGroup> findAll() {
    return optionGroupRepository.findAll().stream()
        .map(categoryMapper::toOptionGroupDomain)
        .collect(Collectors.toList());
  }

  /**
   * {@inheritDoc}
   *
   * <p>Reads {@code option_group.selection_type} directly. The column has been part of the schema
   * since V37 (additive migration); unknown / null values default to {@code SINGLE_CHOICE}.
   */
  @Override
  public Map<Long, String> loadSelectionTypesByIds(Collection<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return Map.of();
    }

    String sql =
        "SELECT id, COALESCE(selection_type, 'SINGLE_CHOICE') AS selection_type "
            + "FROM option_group WHERE id IN (:ids)";
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
    return optionGroupRepository.existsById(id);
  }
}
