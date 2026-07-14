package aros.services.rms.infraestructure.specialselection.persistence.jpa;

import aros.services.rms.core.specialselection.domain.SpecialSelectionHistory;
import aros.services.rms.core.specialselection.port.output.SpecialSelectionHistoryRepositoryPort;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** JPA adapter that implements the special selection history repository port using Spring Data. */
@Component
@RequiredArgsConstructor
public class SpecialSelectionHistoryPersistenceAdapter
    implements SpecialSelectionHistoryRepositoryPort {

  private final SpecialSelectionHistoryRepository historyRepository;
  private final SpecialSelectionMapper mapper;

  @Override
  @Transactional
  public SpecialSelectionHistory save(SpecialSelectionHistory history) {
    var entity = mapper.toHistoryEntity(history);
    var saved = historyRepository.save(entity);
    return mapper.toHistoryDomain(saved);
  }

  @Override
  public Optional<SpecialSelectionHistory> findById(Long id) {
    return historyRepository.findById(id).map(mapper::toHistoryDomain);
  }

  @Override
  public Optional<SpecialSelectionHistory> findByProductIdAndVersion(Long productId, int version) {
    return historyRepository
        .findByProductIdAndVersion(productId, version)
        .map(mapper::toHistoryDomain);
  }

  @Override
  public Optional<SpecialSelectionHistory> findCurrentByProductId(Long productId) {
    return historyRepository
        .findFirstByProductIdAndIsCurrentOrderByVersionDesc(productId, true)
        .map(mapper::toHistoryDomain);
  }

  @Override
  public Page<SpecialSelectionHistory> findByProductId(Long productId, Pageable pageable) {
    return historyRepository.findByProductId(productId, pageable).map(mapper::toHistoryDomain);
  }

  @Override
  public List<SpecialSelectionHistory> findByProductIdAndChangedAtBetween(
      Long productId, LocalDateTime from, LocalDateTime to) {
    return historyRepository.findByProductIdAndChangedAtBetween(productId, from, to).stream()
        .map(mapper::toHistoryDomain)
        .collect(Collectors.toList());
  }

  @Override
  public int findMaxVersionByProductId(Long productId) {
    return historyRepository.findMaxVersionByProductId(productId);
  }

  @Override
  @Transactional
  public void markAllAsNotCurrent(Long productId) {
    historyRepository.markAllAsNotCurrent(productId);
  }
}
