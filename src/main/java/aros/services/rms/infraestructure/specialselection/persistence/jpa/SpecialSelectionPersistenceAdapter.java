package aros.services.rms.infraestructure.specialselection.persistence.jpa;

import aros.services.rms.core.category.port.output.CategoryRepositoryPort;
import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;
import aros.services.rms.core.specialselection.port.output.SpecialSelectionRepositoryPort;
import aros.services.rms.infraestructure.product.persistence.Product;
import aros.services.rms.infraestructure.product.persistence.jpa.ProductRepository;
import aros.services.rms.infraestructure.specialselection.persistence.GroupProductEntity;
import aros.services.rms.infraestructure.specialselection.persistence.SpecialSelectionAdditionEntity;
import aros.services.rms.infraestructure.specialselection.persistence.SpecialSelectionGroupEntity;
import aros.services.rms.infraestructure.specialselection.persistence.SpecialSelectionQuestionEntity;
import aros.services.rms.infraestructure.specialselection.persistence.SpecialSelectionScheduleEntity;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** JPA adapter that implements the special selection repository port using Spring Data. */
@Component
@RequiredArgsConstructor
public class SpecialSelectionPersistenceAdapter implements SpecialSelectionRepositoryPort {

  private final ProductRepository productRepository;
  private final CategoryRepositoryPort categoryRepositoryPort;
  private final SpecialSelectionGroupRepository groupRepository;
  private final GroupProductRepository groupProductRepository;
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
            .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productId));

    // The combo owns the product's master data: the wizard edits name, description and price in
    // the same form, and the dedicated price endpoint must update the product as well.
    if (config.getName() != null) {
      productEntity.setName(config.getName());
    }
    if (config.getDescription() != null) {
      productEntity.setDescription(config.getDescription());
    }
    if (config.getBasePrice() != null) {
      productEntity.setBasePrice(config.getBasePrice());
    }
    productEntity.setSelectionType(
        config.getSelectionType() != null ? config.getSelectionType().name() : "SPECIAL_SELECTION");
    productEntity.setSelectionBaseRecipeEnabled(config.isBaseRecipeEnabled());
    productEntity.setSchedulingRequired(config.isSchedulingRequired());
    productEntity.setActive(config.isActive());
    productRepository.save(productEntity);

    deleteChildEntities(productId);

    // Child rows are fully replaced on every save. Clear the client-echoed ids: reusing the id of a
    // row deleted in this transaction makes Hibernate report a stale object, and the rows no longer
    // exist anyway.
    List<SpecialSelectionGroupEntity> groups =
        mapper.toGroupEntities(productId, config.getGroups());
    groups.forEach(group -> group.setId(null));
    List<SpecialSelectionGroupEntity> savedGroups = groupRepository.saveAll(groups);

    List<Long> savedGroupIds =
        savedGroups.stream().map(SpecialSelectionGroupEntity::getId).collect(Collectors.toList());

    List<GroupProductEntity> productLinks =
        mapper.toGroupProductLinks(productId, config.getGroups(), savedGroupIds);
    if (!productLinks.isEmpty()) {
      groupProductRepository.saveAll(productLinks);
    }

    List<SpecialSelectionAdditionEntity> additions =
        mapper.toAdditionEntities(productId, config.getAdditions());
    additions.forEach(addition -> addition.setId(null));
    if (!additions.isEmpty()) {
      additionRepository.saveAll(additions);
    }

    List<SpecialSelectionQuestionEntity> questions =
        mapper.toQuestionEntities(productId, config.getQuestions());
    questions.forEach(question -> question.setId(null));
    if (!questions.isEmpty()) {
      questionRepository.saveAll(questions);
    }

    List<SpecialSelectionScheduleEntity> schedule =
        mapper.toScheduleEntities(productId, config.getSchedule());
    schedule.forEach(entry -> entry.setId(null));
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
    List<SpecialSelectionGroupEntity> existingGroups = groupRepository.findByProductId(productId);
    if (!existingGroups.isEmpty()) {
      List<Long> groupIds =
          existingGroups.stream().map(SpecialSelectionGroupEntity::getId).toList();
      groupProductRepository.deleteByGroupIdIn(groupIds);
    }
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

    Map<Long, List<Long>> groupProductMap = buildGroupProductMap(groupIds);

    return mapper.toDomain(
        productEntity,
        groupEntities,
        groupProductMap,
        additionEntities,
        questionEntities,
        scheduleEntities);
  }

  private Map<Long, List<Long>> buildGroupProductMap(List<Long> groupIds) {
    if (groupIds.isEmpty()) {
      return Collections.emptyMap();
    }
    List<GroupProductEntity> links = groupProductRepository.findByGroupIdIn(groupIds);
    return links.stream()
        .collect(
            Collectors.groupingBy(
                GroupProductEntity::getGroupId,
                Collectors.mapping(GroupProductEntity::getProductId, Collectors.toList())));
  }
}
