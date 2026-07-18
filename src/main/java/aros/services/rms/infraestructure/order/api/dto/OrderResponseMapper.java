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
import aros.services.rms.infraestructure.order.api.dto.OrderResponse.ClarificationResponse;
import aros.services.rms.infraestructure.order.api.dto.OrderResponse.OrderDetailResponse;
import aros.services.rms.infraestructure.order.api.dto.OrderResponse.SelectedAdditionResponse;
import aros.services.rms.infraestructure.order.api.dto.OrderResponse.SelectedProductResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mapper that builds a fully-resolved {@link OrderResponse} from a domain {@link Order}. Resolves
 * selected products, additions and clarification question texts via repository ports.
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
   * @return the response with combo data resolved
   */
  public OrderResponse toResponse(Order order) {
    if (order == null) {
      return null;
    }

    List<OrderDetailResponse> details =
        order.getDetails() != null
            ? order.getDetails().stream().map(this::toDetailResponse).collect(Collectors.toList())
            : null;

    return new OrderResponse(
        order.getId(),
        order.getDate(),
        order.getStatus() != null ? order.getStatus().name() : null,
        order.getTable() != null ? order.getTable().getId() : null,
        order.getPartySize(),
        order.getOpenTime(),
        order.getCloseTime(),
        details);
  }

  private OrderDetailResponse toDetailResponse(OrderDetail detail) {
    if (detail == null) {
      return null;
    }

    Map<Long, Product> productMap = resolveProducts(detail.getSelectedProductIds());
    SpecialSelectionConfiguration config = resolveSpecialSelectionConfig(detail);

    Map<Long, SpecialSelectionAddition> additionMap = resolveAdditions(config);
    Map<Long, SpecialSelectionQuestion> questionMap = resolveQuestions(config);

    return new OrderDetailResponse(
        detail.getId(),
        detail.getProduct() != null ? detail.getProduct().getId() : null,
        detail.getProduct() != null ? detail.getProduct().getName() : null,
        detail.getUnitPrice() != null ? detail.getUnitPrice().amount().doubleValue() : null,
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
        toClarifications(detail.getClarifications(), questionMap));
  }

  private Map<Long, Product> resolveProducts(List<Long> selectedProductIds) {
    if (selectedProductIds == null || selectedProductIds.isEmpty()) {
      return Collections.emptyMap();
    }
    List<Product> products = productRepositoryPort.findAllById(selectedProductIds);
    return products.stream().collect(Collectors.toMap(Product::getId, p -> p));
  }

  private SpecialSelectionConfiguration resolveSpecialSelectionConfig(OrderDetail detail) {
    if (detail.getProduct() == null) {
      return null;
    }
    Optional<SpecialSelectionConfiguration> configOpt =
        specialSelectionRepositoryPort.findById(detail.getProduct().getId());
    return configOpt.orElse(null);
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
                  product.getBasePrice() != null
                      ? product.getBasePrice().amount().doubleValue()
                      : null);
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
                  addition.getId(), addition.getName(), addition.getExtraPrice());
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
