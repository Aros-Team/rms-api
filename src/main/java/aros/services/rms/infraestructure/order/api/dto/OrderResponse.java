/* (C) 2026 */

package aros.services.rms.infraestructure.order.api.dto;

import aros.services.rms.core.common.money.domain.CurrencyContext;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderDetail;
import aros.services.rms.core.product.domain.ProductOption;
import aros.services.rms.infraestructure.common.dto.MoneyDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** Response DTO for order data. */
@Schema(description = "Response DTO for order data")
public record OrderResponse(
    @Schema(description = "Order ID", example = "1") Long id,
    @Schema(description = "Order date and time", example = "2026-03-08T14:30:00")
        LocalDateTime date,
    @Schema(description = "Order status", example = "QUEUE") String status,
    @Schema(description = "Table ID", example = "1") Long tableId,
    @JsonInclude(JsonInclude.Include.NON_NULL)
        @Schema(description = "Number of guests covered by the order", nullable = true)
        Integer partySize,
    @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @Schema(description = "Time when table occupancy started", nullable = true)
        LocalDateTime openTime,
    @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @Schema(description = "Time when table occupancy ended", nullable = true)
        LocalDateTime closeTime,
    @Schema(
            description = "Order total (sum of line unit prices)",
            example = "{\"amount\":\"37.50\",\"currency\":\"COP\"}")
        MoneyDto total,
    @Schema(description = "List of products in the order") List<OrderDetailResponse> details) {

  /** Detail of a product in the order. */
  @Schema(description = "Detail of a product in the order")
  public record OrderDetailResponse(
      @Schema(description = "Detail ID", example = "1") Long id,
      @Schema(description = "Product ID", example = "1") Long productId,
      @Schema(description = "Product name", example = "Classic Burger") String productName,
      @JsonInclude(JsonInclude.Include.NON_NULL)
          @Schema(
              description = "Preparation area (station) ID derived from the product",
              nullable = true,
              example = "1")
          Long areaId,
      @JsonInclude(JsonInclude.Include.NON_NULL)
          @Schema(
              description = "Preparation area (station) name derived from the product",
              nullable = true,
              example = "Cocina")
          String areaName,
      @Schema(description = "Unit price") MoneyDto unitPrice,
      @JsonInclude(JsonInclude.Include.NON_NULL)
          @Schema(
              description =
                  "Surcharge from EXTRA-category option selections (unitPrice - basePrice)",
              example = "2.50")
          MoneyDto extraCharge,
      @Schema(description = "Special instructions", example = "No onions") String instructions,
      @Schema(description = "Selected product options") List<ProductOptionResponse> selectedOptions,
      @Schema(description = "Selected combo product IDs", example = "[101, 103]")
          List<Long> selectedProductIds,
      @Schema(description = "Resolved selected combo products")
          List<SelectedProductResponse> selectedProducts,
      @Schema(description = "Selected addition IDs", example = "[5, 6]") List<Long> additionIds,
      @Schema(description = "Resolved selected additions")
          List<SelectedAdditionResponse> selectedAdditions,
      @Schema(
              description = "Whether this line is ready at its preparation station",
              example = "false")
          boolean ready,
      @JsonInclude(JsonInclude.Include.NON_NULL)
          @JsonFormat(shape = JsonFormat.Shape.STRING)
          @Schema(description = "Time when the line was marked ready", nullable = true)
          LocalDateTime readyAt,
      @Schema(description = "Clarification answers") List<ClarificationResponse> clarifications) {}

  /** Selected product option. */
  @Schema(description = "Selected product option")
  public record ProductOptionResponse(
      @Schema(description = "Option ID", example = "1") Long id,
      @Schema(description = "Option name", example = "Large") String name,
      @Schema(description = "Category name", example = "Sizes") String categoryName) {}

  /** Resolved selected combo product. */
  @Schema(description = "Resolved selected combo product")
  public record SelectedProductResponse(
      @Schema(description = "Product ID") Long id,
      @Schema(description = "Product name") String name,
      @Schema(description = "Base price") MoneyDto basePrice) {}

  /** Resolved selected addition. */
  @Schema(description = "Resolved selected addition")
  public record SelectedAdditionResponse(
      @Schema(description = "Addition ID") Long id,
      @Schema(description = "Addition name") String name,
      @Schema(description = "Extra price") MoneyDto extraPrice) {}

  /** Clarification answer to a special selection question. */
  @Schema(description = "Clarification answer to a special selection question")
  public record ClarificationResponse(
      @Schema(description = "Question ID") Long questionId,
      @Schema(description = "Question text") String question,
      @Schema(description = "Answer") String answer) {}

  /**
   * Creates a response from a domain object.
   *
   * @param order the order domain
   * @return the response DTO
   */
  public static OrderResponse fromDomain(Order order) {
    if (order == null) {
      return null;
    }

    List<OrderDetail> orderDetails = order.getDetails();

    return new OrderResponse(
        order.getId(),
        order.getDate(),
        order.getStatus() != null ? order.getStatus().name() : null,
        order.getTable() != null ? order.getTable().getId() : null,
        order.getPartySize(),
        order.getOpenTime(),
        order.getCloseTime(),
        calculateTotal(orderDetails),
        orderDetails != null
            ? orderDetails.stream()
                .map(OrderResponse::fromDomainDetail)
                .collect(Collectors.toList())
            : null);
  }

  /**
   * Computes the order total as the sum of every non-null line {@code unitPrice}. The unit price
   * already includes the line's {@code extraCharge}, so surcharges are not added again. When there
   * are no priced details the total is zero in the configured system currency; the currency is
   * taken from the first non-null unit price.
   *
   * @param details the order details (may be null)
   * @return the computed total as a wire DTO
   */
  public static MoneyDto calculateTotal(List<OrderDetail> details) {
    Currency fallbackCurrency = CurrencyContext.get();

    if (details == null || details.isEmpty()) {
      return MoneyDto.of(Money.zero(fallbackCurrency));
    }

    List<Money> prices =
        details.stream()
            .filter(Objects::nonNull)
            .map(OrderDetail::getUnitPrice)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    if (prices.isEmpty()) {
      return MoneyDto.of(Money.zero(fallbackCurrency));
    }

    Currency totalCurrency = prices.get(0).currency();
    Money total = prices.stream().reduce(Money.zero(totalCurrency), Money::plus);
    return MoneyDto.of(total);
  }

  private static OrderDetailResponse fromDomainDetail(OrderDetail detail) {
    if (detail == null) {
      return null;
    }

    return new OrderDetailResponse(
        detail.getId(),
        detail.getProduct() != null ? detail.getProduct().getId() : null,
        detail.getProduct() != null ? detail.getProduct().getName() : null,
        detail.getProduct() != null ? detail.getProduct().getPreparationAreaId() : null,
        detail.getProduct() != null ? detail.getProduct().getPreparationAreaName() : null,
        detail.getUnitPrice() != null ? MoneyDto.of(detail.getUnitPrice()) : null,
        detail.getExtraCharge() != null ? MoneyDto.of(detail.getExtraCharge()) : null,
        detail.getInstructions(),
        detail.getSelectedOptions() != null
            ? detail.getSelectedOptions().stream()
                .map(OrderResponse::fromDomainOption)
                .collect(Collectors.toList())
            : null,
        detail.getSelectedProductIds(),
        null,
        detail.getAdditionIds(),
        null,
        detail.isReady(),
        detail.getReadyAt(),
        detail.getClarifications() != null
            ? detail.getClarifications().stream()
                .map(ca -> new ClarificationResponse(ca.getQuestionId(), null, ca.getAnswer()))
                .collect(Collectors.toList())
            : null);
  }

  private static ProductOptionResponse fromDomainOption(ProductOption option) {
    if (option == null) {
      return null;
    }

    return new ProductOptionResponse(
        option.getId(),
        option.getName(),
        option.getCategory() != null ? option.getCategory().getName() : null);
  }
}
