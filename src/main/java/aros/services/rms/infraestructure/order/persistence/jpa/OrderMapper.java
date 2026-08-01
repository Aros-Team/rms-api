/* (C) 2026 */

package aros.services.rms.infraestructure.order.persistence.jpa;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderDetail;
import aros.services.rms.infraestructure.area.persistence.jpa.Area;
import aros.services.rms.infraestructure.order.persistence.OrderDetailOption;
import aros.services.rms.infraestructure.product.persistence.jpa.ProductMapper;
import aros.services.rms.infraestructure.table.persistence.jpa.TableMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Mapper for orders. */
@Component
@RequiredArgsConstructor
public class OrderMapper {

  private final TableMapper tableMapper;
  private final ProductMapper productMapper;

  /**
   * Converts a domain to entity.
   *
   * @param domain the domain
   * @return the entity
   */
  public aros.services.rms.infraestructure.order.persistence.Order toEntity(Order domain) {
    if (domain == null) {
      return null;
    }

    aros.services.rms.infraestructure.order.persistence.Order entity =
        aros.services.rms.infraestructure.order.persistence.Order.builder()
            .id(domain.getId())
            .date(domain.getDate())
            .status(
                domain.getStatus() != null
                    ? aros.services.rms.infraestructure.order.persistence.OrderStatus.valueOf(
                        domain.getStatus().name())
                    : null)
            .table(tableMapper.toEntity(domain.getTable()))
            .partySize(domain.getPartySize())
            .openTime(domain.getOpenTime())
            .closeTime(domain.getCloseTime())
            .build();

    if (domain.getDetails() != null) {
      entity.setDetails(
          domain.getDetails().stream()
              .map(detail -> toOrderDetailEntity(detail, entity))
              .collect(Collectors.toList()));
    }

    if (domain.getPreparationAreaIds() != null) {
      entity.setPreparationAreas(
          domain.getPreparationAreaIds().stream()
              .map(areaId -> Area.builder().id(areaId).build())
              .collect(Collectors.toSet()));
    }

    return entity;
  }

  /**
   * Converts an order detail domain to entity.
   *
   * @param domain the domain
   * @param orderEntity the order entity
   * @return the entity
   */
  public aros.services.rms.infraestructure.order.persistence.OrderDetail toOrderDetailEntity(
      OrderDetail domain, aros.services.rms.infraestructure.order.persistence.Order orderEntity) {
    if (domain == null) {
      return null;
    }

    aros.services.rms.infraestructure.order.persistence.OrderDetail entity =
        aros.services.rms.infraestructure.order.persistence.OrderDetail.builder()
            .id(domain.getId())
            .order(orderEntity)
            .product(productMapper.toProductEntity(domain.getProduct()))
            .unitPrice(domain.getUnitPrice().amount().doubleValue())
            .instructions(domain.getInstructions())
            .build();

    entity.setSelectedOptions(toOrderDetailOptionEntities(domain, entity));

    return entity;
  }

  /**
   * Builds the {@code order_detail_options} join rows from the domain selection list. The per-row
   * {@code extra_price} is read from {@link OrderDetail#getOptionExtraPrices()}; missing keys
   * default to 0.
   */
  private List<OrderDetailOption> toOrderDetailOptionEntities(
      OrderDetail domain,
      aros.services.rms.infraestructure.order.persistence.OrderDetail orderDetailEntity) {
    if (domain.getSelectedOptions() == null || domain.getSelectedOptions().isEmpty()) {
      return new ArrayList<>();
    }
    Map<Long, Money> extraPrices =
        domain.getOptionExtraPrices() == null ? Map.of() : domain.getOptionExtraPrices();
    List<OrderDetailOption> rows = new ArrayList<>(domain.getSelectedOptions().size());
    for (aros.services.rms.core.product.domain.ProductOption option : domain.getSelectedOptions()) {
      if (option == null || option.getId() == null) {
        continue;
      }
      Money surcharge = extraPrices.get(option.getId());
      double extra = surcharge == null ? 0.0 : surcharge.amount().doubleValue();
      rows.add(
          OrderDetailOption.builder()
              .orderDetail(orderDetailEntity)
              .option(productMapper.toProductOptionEntity(option))
              .extraPrice(extra)
              .build());
    }
    return rows;
  }

  /**
   * Converts an entity to domain.
   *
   * @param entity the entity
   * @return the domain
   */
  public Order toDomain(aros.services.rms.infraestructure.order.persistence.Order entity) {
    if (entity == null) {
      return null;
    }

    Set<Long> areaIds =
        entity.getPreparationAreas() != null
            ? entity.getPreparationAreas().stream().map(Area::getId).collect(Collectors.toSet())
            : null;

    return Order.builder()
        .id(entity.getId())
        .date(entity.getDate())
        .status(
            entity.getStatus() != null
                ? aros.services.rms.core.order.domain.OrderStatus.valueOf(entity.getStatus().name())
                : null)
        .table(tableMapper.toDomain(entity.getTable()))
        .partySize(entity.getPartySize())
        .openTime(entity.getOpenTime())
        .closeTime(entity.getCloseTime())
        .details(
            entity.getDetails() != null
                ? entity.getDetails().stream()
                    .map(this::toOrderDetailDomain)
                    .collect(Collectors.toList())
                : null)
        .preparationAreaIds(areaIds)
        .build();
  }

  /**
   * Converts an order detail entity to domain.
   *
   * @param entity the entity
   * @return the domain
   */
  public OrderDetail toOrderDetailDomain(
      aros.services.rms.infraestructure.order.persistence.OrderDetail entity) {
    if (entity == null) {
      return null;
    }

    List<OrderDetailOption> rows =
        entity.getSelectedOptions() != null ? entity.getSelectedOptions() : List.of();

    List<aros.services.rms.core.product.domain.ProductOption> selectedOptions =
        rows.stream()
            .map(OrderDetailOption::getOption)
            .map(productMapper::toProductOptionDomain)
            .collect(Collectors.toList());

    Money unitPrice =
        entity.getUnitPrice() != null
            ? new Money(BigDecimal.valueOf(entity.getUnitPrice()), Currency.getInstance("COP"))
            : Money.zero(Currency.getInstance("COP"));

    Currency currency = unitPrice.currency();
    Money extraCharge = Money.zero(currency);
    Map<Long, Money> optionExtraPrices = new HashMap<>();
    for (OrderDetailOption row : rows) {
      Double rowExtra = row.getExtraPrice();
      double value = rowExtra == null ? 0.0 : rowExtra;
      extraCharge = extraCharge.plus(new Money(BigDecimal.valueOf(value), currency));
      if (row.getOption() != null && row.getOption().getId() != null) {
        optionExtraPrices.put(
            row.getOption().getId(), new Money(BigDecimal.valueOf(value), currency));
      }
    }

    return OrderDetail.builder()
        .id(entity.getId())
        .product(productMapper.toProductDomain(entity.getProduct()))
        .unitPrice(unitPrice)
        .extraCharge(extraCharge)
        .optionExtraPrices(optionExtraPrices)
        .instructions(entity.getInstructions())
        .selectedOptions(selectedOptions)
        .build();
  }
}
