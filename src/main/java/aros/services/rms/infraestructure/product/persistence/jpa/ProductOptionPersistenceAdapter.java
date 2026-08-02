/* (C) 2026 */

package aros.services.rms.infraestructure.product.persistence.jpa;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.inventory.domain.ProductRecipe;
import aros.services.rms.core.product.domain.ProductOption;
import aros.services.rms.core.product.domain.ProductOptionCostProfile;
import aros.services.rms.core.product.port.output.ProductOptionRepositoryPort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Persistence adapter that implements ProductOptionRepositoryPort using JPA. */
@Component
@RequiredArgsConstructor
public class ProductOptionPersistenceAdapter implements ProductOptionRepositoryPort {

  private final ProductOptionRepository productOptionRepository;
  private final ProductMapper productMapper;
  private final EntityManager entityManager;

  @Override
  public ProductOption save(ProductOption productOption) {
    aros.services.rms.infraestructure.product.persistence.ProductOption entity =
        productMapper.toProductOptionEntity(productOption);
    aros.services.rms.infraestructure.product.persistence.ProductOption savedEntity =
        productOptionRepository.save(entity);
    return productMapper.toProductOptionDomain(savedEntity);
  }

  @Override
  public Optional<ProductOption> findById(Long id) {
    return productOptionRepository.findById(id).map(productMapper::toProductOptionDomain);
  }

  @Override
  public List<ProductOption> findAllById(List<Long> ids) {
    return productOptionRepository.findAllById(ids).stream()
        .map(productMapper::toProductOptionDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<ProductOption> findAll() {
    return productOptionRepository.findAll().stream()
        .map(productMapper::toProductOptionDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<ProductOption> findByProductId(Long productId) {
    return productOptionRepository.findByProductId(productId).stream()
        .map(productMapper::toProductOptionDomain)
        .collect(Collectors.toList());
  }

  /**
   * {@inheritDoc}
   *
   * <p>Reads {@code option_group.selection_type}, {@code option_group.replace_supply_category_id}
   * directly. The columns have been part of the schema since V37 (additive migration); unknown /
   * null selection type defaults to {@code SINGLE_CHOICE}.
   */
  @Override
  public List<ProductOptionCostProfile> loadCostProfilesByProductId(Long productId) {
    String sql =
        """
        SELECT po.id, po.name, oc.id, oc.name, COALESCE(ppo.extra_price, 0),
          COALESCE(oc.selection_type, 'SINGLE_CHOICE') AS selection_type,
          oc.replace_supply_category_id,
          COALESCE(default_cost.material_cost, 0) AS default_slot_cost
        FROM product_product_options ppo
        JOIN product_options po ON po.id = ppo.option_id
        LEFT JOIN option_group oc ON oc.id = po.option_category_id
        LEFT JOIN ( \
            SELECT pr.product_id, s.supply_category_id, \
                   SUM(pr.required_quantity * sv.unit_cost) AS material_cost \
            FROM product_recipes pr \
            JOIN supply_variants sv ON sv.id = pr.supply_variant_id \
            JOIN supplies s ON s.id = sv.supply_id \
            GROUP BY pr.product_id, s.supply_category_id \
        ) default_cost \
          ON default_cost.product_id = ppo.product_id \
         AND default_cost.supply_category_id = oc.replace_supply_category_id
        WHERE ppo.product_id = :productId
        ORDER BY ppo.display_order, po.id
        """;

    Query query = entityManager.createNativeQuery(sql).setParameter("productId", productId);
    List<?> rows = query.getResultList();
    return rows.stream().map(this::toCostProfile).toList();
  }

  private ProductOptionCostProfile toCostProfile(Object rawRow) {
    Object[] row = (Object[]) rawRow;
    return new ProductOptionCostProfile(
        toLong(row[0]),
        (String) row[1],
        toLong(row[2]),
        (String) row[3],
        toMoney(row[4]),
        normalizeSelectionType(row[5] == null ? null : row[5].toString()),
        toLong(row[6]),
        toMoney(row[7]));
  }

  private Long toLong(Object value) {
    return value == null ? null : ((Number) value).longValue();
  }

  private Money toMoney(Object value) {
    BigDecimal amount = value == null ? BigDecimal.ZERO : new BigDecimal(value.toString());
    return new Money(amount, Currency.getInstance("COP"));
  }

  private static String normalizeSelectionType(String stored) {
    if (stored == null || stored.isBlank()) {
      return aros.services.rms.core.category.domain.OptionSelectionType.SINGLE_CHOICE.name();
    }
    try {
      return aros.services.rms.core.category.domain.OptionSelectionType.valueOf(stored).name();
    } catch (IllegalArgumentException unknown) {
      return aros.services.rms.core.category.domain.OptionSelectionType.SINGLE_CHOICE.name();
    }
  }

  @Override
  @Transactional
  public void associateOptionsToProduct(Long productId, List<Long> optionIds) {
    for (Long optionId : optionIds) {
      productOptionRepository.associateOptionToProduct(productId, optionId);
    }
  }

  @Override
  @Transactional
  public void upsertOptionAssociation(
      Long productId, Long optionId, BigDecimal extraPrice, int displayOrder) {
    productOptionRepository.upsertOptionAssociation(productId, optionId, extraPrice, displayOrder);
  }

  @Override
  @Transactional
  public void removeAllOptionsFromProduct(Long productId) {
    productOptionRepository.removeAllOptionsFromProduct(productId);
  }

  @Override
  public boolean isOptionAssociatedWithProduct(Long productId, Long optionId) {
    Long count = productOptionRepository.isOptionAssociatedWithProduct(productId, optionId);
    return count != null && count > 0;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Joins {@code product_recipes → supply_variants → supplies} and filters by the given supply
   * category. Returns the matching {@code (variantId, requiredQuantity)} rows so the caller can
   * subtract them from inventory when a SINGLE_CHOICE substitution option is selected.
   */
  @Override
  public List<ProductRecipe> loadBaseRecipeBySupplyCategory(Long productId, Long supplyCategoryId) {
    if (productId == null || supplyCategoryId == null) {
      return List.of();
    }
    String sql =
        """
        SELECT pr.product_id, pr.supply_variant_id, pr.required_quantity
        FROM product_recipes pr
        JOIN supply_variants sv ON sv.id = pr.supply_variant_id
        JOIN supplies s ON s.id = sv.supply_id
        WHERE pr.product_id = :productId
          AND s.supply_category_id = :supplyCategoryId
        """;
    Query query =
        entityManager
            .createNativeQuery(sql)
            .setParameter("productId", productId)
            .setParameter("supplyCategoryId", supplyCategoryId);
    @SuppressWarnings("unchecked")
    List<Object[]> rows = query.getResultList();
    List<ProductRecipe> result = new ArrayList<>(rows.size());
    for (Object[] row : rows) {
      Long pid = toLong(row[0]);
      Long variantId = toLong(row[1]);
      Object qtyObj = row[2];
      BigDecimal qty = qtyObj == null ? BigDecimal.ZERO : new BigDecimal(qtyObj.toString());
      if (variantId == null) {
        continue;
      }
      result.add(
          ProductRecipe.builder()
              .productId(pid)
              .supplyVariantId(variantId)
              .requiredQuantity(qty)
              .build());
    }
    return List.copyOf(result);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Joins {@code product_recipes → supply_variants → supplies}, groups by product and supply
   * category, and returns a nested map. The SQL is the same shape as the {@code default_cost}
   * sub-query used in {@link #loadCostProfilesByProductId(Long)} but exposed independently so
   * analytics callers (e.g. menu engineering substitution aggregation) can fetch the map in one
   * round-trip.
   */
  @Override
  public Map<Long, Map<Long, Money>> loadDefaultSlotCostByProductAndCategory() {
    String sql =
        """
        SELECT pr.product_id, s.supply_category_id,
               SUM(pr.required_quantity * sv.unit_cost) AS slot_cost
        FROM product_recipes pr
        JOIN supply_variants sv ON sv.id = pr.supply_variant_id
        JOIN supplies s ON s.id = sv.supply_id
        GROUP BY pr.product_id, s.supply_category_id
        """;
    Query query = entityManager.createNativeQuery(sql);
    @SuppressWarnings("unchecked")
    List<Object[]> rows = query.getResultList();
    Map<Long, Map<Long, Money>> result = new HashMap<>();
    for (Object[] row : rows) {
      Long productId = toLong(row[0]);
      Long supplyCategoryId = toLong(row[1]);
      if (productId == null || supplyCategoryId == null) {
        continue;
      }
      Object costObj = row[2];
      BigDecimal cost = costObj == null ? BigDecimal.ZERO : new BigDecimal(costObj.toString());
      result
          .computeIfAbsent(productId, k -> new HashMap<>())
          .put(supplyCategoryId, new Money(cost, Currency.getInstance("COP")));
    }
    return result;
  }

  @Override
  public Map<Long, Map<Long, List<ProductOption>>> loadOptionsByProductAndGroup(
      java.util.Collection<Long> productIds) {
    if (productIds == null || productIds.isEmpty()) {
      return Map.of();
    }
    String sql =
        """
        SELECT ppo.product_id, po.option_category_id, po.id, po.name
        FROM product_product_options ppo
        JOIN product_options po ON po.id = ppo.option_id
        WHERE ppo.product_id IN (:productIds)
        ORDER BY ppo.product_id, po.option_category_id, ppo.display_order, po.id
        """;
    Query query = entityManager.createNativeQuery(sql).setParameter("productIds", productIds);
    @SuppressWarnings("unchecked")
    List<Object[]> rows = query.getResultList();
    Map<Long, Map<Long, List<ProductOption>>> result = new HashMap<>();
    for (Object[] row : rows) {
      Long pid = toLong(row[0]);
      Long groupId = toLong(row[1]);
      Long optionId = toLong(row[2]);
      String optionName = (String) row[3];
      if (pid == null || groupId == null) {
        continue;
      }
      result
          .computeIfAbsent(pid, k -> new HashMap<>())
          .computeIfAbsent(groupId, k -> new ArrayList<>())
          .add(ProductOption.builder().id(optionId).name(optionName).build());
    }
    return result;
  }
}
