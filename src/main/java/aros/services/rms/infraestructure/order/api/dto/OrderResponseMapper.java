/* (C) 2026 */

package aros.services.rms.infraestructure.order.api.dto;

import aros.services.rms.core.order.domain.ClarificationAnswer;
import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderDetail;
import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.port.output.ProductRepositoryPort;
import aros.services.rms.core.specialselection.domain.SpecialSelectionAddition;
import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;
import aros.services.rms.core.specialselection.domain.SpecialSelectionQuestion;
import aros.services.rms.core.specialselection.port.output.SpecialSelectionRepositoryPort;
import aros.services.rms.infraestructure.common.dto.MoneyDto;
import aros.services.rms.infraestructure.order.api.dto.OrderResponse.ClarificationResponse;
import aros.services.rms.infraestructure.order.api.dto.OrderResponse.OrderDetailResponse;
import aros.services.rms.infraestructure.order.api.dto.OrderResponse.SelectedAdditionResponse;
import aros.services.rms.infraestructure.order.api.dto.OrderResponse.SelectedProductResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mapper that builds a fully-resolved {@link OrderResponse} from a domain {@link Order}. Resolves
 * selected products, additions, clarification question texts and each line's preparation area via
 * repository ports.
 *
 * <p>Products (and therefore their preparation area) are batch-loaded once per {@link
 * #toResponses(List) call}, so an order page with many lines does not issue one product query per
 * detail. The batch lookup is keyed by product id and also covers the products selected inside
 * combo lines.
 */
@Component
@RequiredArgsConstructor
public class OrderResponseMapper {

  private final ProductRepositoryPort productRepositoryPort;
  private final SpecialSelectionRepositoryPort specialSelectionRepositoryPort;

  /**
   * Converts a domain order into a fully-resolved response DTO.
   *
   * @param order the domain order
   * @return the response with combo data and per-line preparation areas resolved
   */
  public OrderResponse toResponse(Order order) {
    if (order == null) {
      return null;
    }
    List<OrderResponse> responses = toResponses(List.of(order));
    return responses.isEmpty() ? null : responses.get(0);
  }

  /**
   * Converts a list of domain orders into response DTOs, batch-loading every referenced product
   * (order lines and combo selections) in a single call.
   *
   * @param orders the domain orders
   * @return the responses in the same order as the input
   */
  public List<OrderResponse> toResponses(List<Order> orders) {
    if (orders == null) {
      return null;
    }

    Map<Long, Product> productMap = batchLoadProducts(orders);
    Map<Long, Optional<SpecialSelectionConfiguration>> configCache = new HashMap<>();

    return orders.stream()
        .map(order -> mapOrder(order, productMap, configCache))
        .collect(Collectors.toList());
  }

  private OrderResponse mapOrder(
      Order order,
      Map<Long, Product> productMap,
      Map<Long, Optional<SpecialSelectionConfiguration>> configCache) {
    if (order == null) {
      return null;
    }

    List<OrderDetailResponse> details =
        order.getDetails() != null
            ? order.getDetails().stream()
                .map(detail -> toDetailResponse(detail, productMap, configCache))
                .collect(Collectors.toList())
            : null;

    return new OrderResponse(
        order.getId(),
        order.getDate(),
        order.getStatus() != null ? order.getStatus().name() : null,
        order.getTable() != null ? order.getTable().getId() : null,
        order.getPartySize(),
        order.getOpenTime(),
        order.getCloseTime(),
        order.getPreparedAt(),
        order.getDeliveredAt(),
        OrderResponse.calculateTotal(order.getDetails()),
        details);
  }

  /**
   * Loads every product referenced by the given orders (line products plus combo selections) in a
   * single repository call.
   */
  private Map<Long, Product> batchLoadProducts(List<Order> orders) {
    Set<Long> productIds = new LinkedHashSet<>();
    for (Order order : orders) {
      if (order == null || order.getDetails() == null) {
        continue;
      }
      for (OrderDetail detail : order.getDetails()) {
        if (detail == null) {
          continue;
        }
        if (detail.getProduct() != null && detail.getProduct().getId() != null) {
          productIds.add(detail.getProduct().getId());
        }
        if (detail.getSelectedProductIds() != null) {
          detail.getSelectedProductIds().stream().filter(id -> id != null).forEach(productIds::add);
        }
      }
    }

    if (productIds.isEmpty()) {
      return Collections.emptyMap();
    }

    return productRepositoryPort.findAllById(new ArrayList<>(productIds)).stream()
        .filter(product -> product != null && product.getId() != null)
        .collect(Collectors.toMap(Product::getId, product -> product, (first, second) -> first));
  }

  private OrderDetailResponse toDetailResponse(
      OrderDetail detail,
      Map<Long, Product> productMap,
      Map<Long, Optional<SpecialSelectionConfiguration>> configCache) {
    if (detail == null) {
      return null;
    }

    Product product = detail.getProduct();
    Long productId = product != null ? product.getId() : null;
    Product resolvedProduct = productId != null ? productMap.get(productId) : null;
    SpecialSelectionConfiguration config = resolveSpecialSelectionConfig(productId, configCache);

    Map<Long, SpecialSelectionAddition> additionMap = resolveAdditions(config);
    Map<Long, SpecialSelectionQuestion> questionMap = resolveQuestions(config);

    return new OrderDetailResponse(
        detail.getId(),
        productId,
        product != null ? product.getName() : null,
        resolvedProduct != null
            ? resolvedProduct.getPreparationAreaId()
            : (product != null ? product.getPreparationAreaId() : null),
        resolvedProduct != null
            ? resolvedProduct.getPreparationAreaName()
            : (product != null ? product.getPreparationAreaName() : null),
        detail.getUnitPrice() != null ? MoneyDto.of(detail.getUnitPrice()) : null,
        detail.getExtraCharge() != null ? MoneyDto.of(detail.getExtraCharge()) : null,
        detail.getInstructions(),
        detail.getSelectedOptions() != null
            ? detail.getSelectedOptions().stream()
                .map(
                    opt ->
                        new OrderResponse.ProductOptionResponse(
                            opt.getId(),
                            opt.getName(),
                            opt.getCategory() != null ? opt.getCategory().getName() : null))
                .collect(Collectors.toList())
            : null,
        detail.getSelectedProductIds(),
        toSelectedProducts(detail.getSelectedProductIds(), productMap),
        detail.getAdditionIds(),
        toSelectedAdditions(detail.getAdditionIds(), additionMap),
        detail.isReady(),
        detail.getReadyAt(),
        detail.getPreparationStartedAt(),
        detail.isDelivered(),
        detail.getDeliveredAt(),
        toClarifications(detail.getClarifications(), questionMap));
  }

  private SpecialSelectionConfiguration resolveSpecialSelectionConfig(
      Long productId, Map<Long, Optional<SpecialSelectionConfiguration>> configCache) {
    if (productId == null) {
      return null;
    }
    Optional<SpecialSelectionConfiguration> config =
        configCache.computeIfAbsent(productId, specialSelectionRepositoryPort::findById);
    return config != null ? config.orElse(null) : null;
  }

  private Map<Long, SpecialSelectionAddition> resolveAdditions(
      SpecialSelectionConfiguration config) {
    if (config == null || config.getAdditions() == null) {
      return Collections.emptyMap();
    }
    return config.getAdditions().stream()
        .collect(Collectors.toMap(SpecialSelectionAddition::getId, a -> a));
  }

  private Map<Long, SpecialSelectionQuestion> resolveQuestions(
      SpecialSelectionConfiguration config) {
    if (config == null || config.getQuestions() == null) {
      return Collections.emptyMap();
    }
    return config.getQuestions().stream()
        .collect(Collectors.toMap(SpecialSelectionQuestion::getId, q -> q));
  }

  private List<SelectedProductResponse> toSelectedProducts(
      List<Long> selectedProductIds, Map<Long, Product> productMap) {
    if (selectedProductIds == null || selectedProductIds.isEmpty()) {
      return List.of();
    }
    return selectedProductIds.stream()
        .map(
            id -> {
              Product product = productMap.get(id);
              if (product == null) {
                return new SelectedProductResponse(id, null, null);
              }
              return new SelectedProductResponse(
                  product.getId(),
                  product.getName(),
                  product.getBasePrice() != null ? MoneyDto.of(product.getBasePrice()) : null);
            })
        .collect(Collectors.toList());
  }

  private List<SelectedAdditionResponse> toSelectedAdditions(
      List<Long> additionIds, Map<Long, SpecialSelectionAddition> additionMap) {
    if (additionIds == null || additionIds.isEmpty()) {
      return List.of();
    }
    return additionIds.stream()
        .map(
            id -> {
              SpecialSelectionAddition addition = additionMap.get(id);
              if (addition == null) {
                return new SelectedAdditionResponse(id, null, null);
              }
              return new SelectedAdditionResponse(
                  addition.getId(),
                  addition.getName(),
                  addition.getExtraPrice() != null
                      ? MoneyDto.of(
                          BigDecimal.valueOf(addition.getExtraPrice())
                              .setScale(2, java.math.RoundingMode.HALF_UP))
                      : null);
            })
        .collect(Collectors.toList());
  }

  private List<ClarificationResponse> toClarifications(
      List<ClarificationAnswer> clarifications, Map<Long, SpecialSelectionQuestion> questionMap) {
    if (clarifications == null || clarifications.isEmpty()) {
      return List.of();
    }
    return clarifications.stream()
        .map(
            ca -> {
              SpecialSelectionQuestion question = questionMap.get(ca.getQuestionId());
              String questionText = question != null ? question.getQuestion() : null;
              return new ClarificationResponse(ca.getQuestionId(), questionText, ca.getAnswer());
            })
        .collect(Collectors.toList());
  }
}
