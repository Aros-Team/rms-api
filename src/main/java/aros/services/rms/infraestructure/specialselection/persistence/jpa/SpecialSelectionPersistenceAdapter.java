package aros.services.rms.infraestructure.specialselection.persistence.jpa;

import aros.services.rms.core.product.domain.ProductOption;
import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;
import aros.services.rms.core.specialselection.port.output.SpecialSelectionRepositoryPort;
import aros.services.rms.infraestructure.product.persistence.Product;
import aros.services.rms.infraestructure.product.persistence.jpa.ProductOptionRepository;
import aros.services.rms.infraestructure.product.persistence.jpa.ProductRepository;
import aros.services.rms.infraestructure.specialselection.persistence.ProductProductOptionEntity;
import aros.services.rms.infraestructure.specialselection.persistence.SpecialSelectionAdditionEntity;
import aros.services.rms.infraestructure.specialselection.persistence.SpecialSelectionGroupEntity;
import aros.services.rms.infraestructure.specialselection.persistence.SpecialSelectionQuestionEntity;
import aros.services.rms.infraestructure.specialselection.persistence.SpecialSelectionScheduleEntity;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** JPA adapter that implements the special selection repository port using Spring Data. */
@Component
@RequiredArgsConstructor
public class SpecialSelectionPersistenceAdapter implements SpecialSelectionRepositoryPort {

  private final ProductRepository productRepository;
  private final ProductOptionRepository productOptionRepository;
  private final SpecialSelectionGroupRepository groupRepository;
  private final ProductProductOptionRepository groupOptionRepository;
  private final SpecialSelectionAdditionRepository additionRepository;
  private final SpecialSelectionQuestionRepository questionRepository;
  private final SpecialSelectionScheduleRepository scheduleRepository;
  private final SpecialSelectionMapper mapper;

  @Override
  @Transactional
  public SpecialSelectionConfiguration save(SpecialSelectionConfiguration config) {
    Long productId = config.getProductId();

    Product productEntity =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

    productEntity.setSelectionType(
        config.getSelectionType() != null ? config.getSelectionType().name() : "SPECIAL_SELECTION");
    productEntity.setSelectionBaseRecipeEnabled(config.isBaseRecipeEnabled());
    productEntity.setSchedulingRequired(config.isSchedulingRequired());
    productEntity.setActive(config.isActive());
    productRepository.save(productEntity);

    deleteChildEntities(productId);

    List<SpecialSelectionGroupEntity> groups =
        mapper.toGroupEntities(productId, config.getGroups());
    List<SpecialSelectionGroupEntity> savedGroups = groupRepository.saveAll(groups);

    Map<Long, Long> groupIdMapping = buildGroupIdMapping(config.getGroups(), savedGroups);

    List<ProductProductOptionEntity> optionLinks =
        mapper.toGroupOptionLinks(productId, config.getGroups(), groupIdMapping);
    if (!optionLinks.isEmpty()) {
      groupOptionRepository.saveAll(optionLinks);
    }

    List<SpecialSelectionAdditionEntity> additions =
        mapper.toAdditionEntities(productId, config.getAdditions());
    if (!additions.isEmpty()) {
      additionRepository.saveAll(additions);
    }

    List<SpecialSelectionQuestionEntity> questions =
        mapper.toQuestionEntities(productId, config.getQuestions());
    if (!questions.isEmpty()) {
      questionRepository.saveAll(questions);
    }

    List<SpecialSelectionScheduleEntity> schedule =
        mapper.toScheduleEntities(productId, config.getSchedule());
    if (!schedule.isEmpty()) {
      scheduleRepository.saveAll(schedule);
    }

    return findById(productId).orElse(config);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<SpecialSelectionConfiguration> findById(Long productId) {
    Optional<Product> productOpt = productRepository.findById(productId);
    if (productOpt.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(buildConfiguration(productOpt.get()));
  }

  @Override
  @Transactional(readOnly = true)
  public List<SpecialSelectionConfiguration> findAll() {
    return productRepository.findAll().stream()
        .filter(p -> "SPECIAL_SELECTION".equals(p.getSelectionType()))
        .map(this::buildConfiguration)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<SpecialSelectionConfiguration> findAllActive() {
    return productRepository.findAll().stream()
        .filter(p -> "SPECIAL_SELECTION".equals(p.getSelectionType()) && p.isActive())
        .map(this::buildConfiguration)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void deleteSoft(Long productId) {
    productRepository
        .findById(productId)
        .ifPresent(
            p -> {
              p.setActive(false);
              productRepository.save(p);
            });
  }

  @Override
  public boolean existsById(Long productId) {
    return productRepository.existsById(productId);
  }

  private void deleteChildEntities(Long productId) {
    groupOptionRepository.deleteByProductId(productId);
    groupRepository.deleteByProductId(productId);
    additionRepository.deleteByProductId(productId);
    questionRepository.deleteByProductId(productId);
    scheduleRepository.deleteByProductId(productId);
  }

  private SpecialSelectionConfiguration buildConfiguration(Product productEntity) {
    Long productId = productEntity.getId();

    List<SpecialSelectionGroupEntity> groupEntities = groupRepository.findByProductId(productId);
    List<SpecialSelectionAdditionEntity> additionEntities =
        additionRepository.findByProductId(productId);
    List<SpecialSelectionQuestionEntity> questionEntities =
        questionRepository.findByProductId(productId);
    List<SpecialSelectionScheduleEntity> scheduleEntities =
        scheduleRepository.findByProductId(productId);

    List<Long> groupIds =
        groupEntities.stream().map(SpecialSelectionGroupEntity::getId).collect(Collectors.toList());
    List<ProductProductOptionEntity> groupOptionLinks =
        groupIds.isEmpty()
            ? Collections.emptyList()
            : groupOptionRepository.findAll().stream()
                .filter(
                    l ->
                        l.getSelectionGroupId() != null
                            && groupIds.contains(l.getSelectionGroupId()))
                .collect(Collectors.toList());

    Set<Long> allOptionIds = collectAllOptionIds(groupOptionLinks, additionEntities);
    List<ProductOption> allOptions =
        allOptionIds.isEmpty()
            ? Collections.emptyList()
            : productOptionRepository.findAllById(allOptionIds).stream()
                .map(p -> ProductOption.builder().id(p.getId()).name(p.getName()).build())
                .collect(Collectors.toList());

    return mapper.toDomain(
        productEntity,
        groupEntities,
        groupOptionLinks,
        additionEntities,
        questionEntities,
        scheduleEntities,
        allOptions);
  }

  private Set<Long> collectAllOptionIds(
      List<ProductProductOptionEntity> groupOptionLinks,
      List<SpecialSelectionAdditionEntity> additionEntities) {
    Set<Long> ids = new java.util.HashSet<>();
    for (ProductProductOptionEntity link : groupOptionLinks) {
      if (link.getOptionId() != null) {
        ids.add(link.getOptionId());
      }
    }
    for (SpecialSelectionAdditionEntity ae : additionEntities) {
      if (ae.getOptionId() != null) {
        ids.add(ae.getOptionId());
      }
    }
    return ids;
  }

  private Map<Long, Long> buildGroupIdMapping(
      List<aros.services.rms.core.specialselection.domain.SpecialSelectionGroup> domainGroups,
      List<SpecialSelectionGroupEntity> savedEntities) {
    Map<Long, Long> mapping = new HashMap<>();
    for (int i = 0; i < Math.min(domainGroups.size(), savedEntities.size()); i++) {
      Long domainId = domainGroups.get(i).getId();
      Long savedId = savedEntities.get(i).getId();
      if (domainId != null) {
        mapping.put(domainId, savedId);
      }
    }
    return mapping;
  }
}
