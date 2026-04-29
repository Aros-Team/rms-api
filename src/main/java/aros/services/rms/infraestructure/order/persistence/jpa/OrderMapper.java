/* (C) 2026 */
package aros.services.rms.infraestructure.order.persistence.jpa;

import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderDetail;
import aros.services.rms.infraestructure.area.persistence.jpa.Area;
import aros.services.rms.infraestructure.product.persistence.jpa.ProductMapper;
import aros.services.rms.infraestructure.table.persistence.jpa.TableMapper;
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
    if (domain == null) return null;

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
    if (domain == null) return null;

    return aros.services.rms.infraestructure.order.persistence.OrderDetail.builder()
        .id(domain.getId())
        .order(orderEntity)
        .product(productMapper.toProductEntity(domain.getProduct()))
        .unitPrice(domain.getUnitPrice())
        .instructions(domain.getInstructions())
        .selectedOptions(
            domain.getSelectedOptions() != null
                ? domain.getSelectedOptions().stream()
                    .map(productMapper::toProductOptionEntity)
                    .collect(Collectors.toList())
                : null)
        .build();
  }

  /**
   * Converts an entity to domain.
   *
   * @param entity the entity
   * @return the domain
   */
  public Order toDomain(aros.services.rms.infraestructure.order.persistence.Order entity) {
    if (entity == null) return null;

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
    if (entity == null) return null;

    return OrderDetail.builder()
        .id(entity.getId())
        .product(productMapper.toProductDomain(entity.getProduct()))
        .unitPrice(entity.getUnitPrice())
        .instructions(entity.getInstructions())
        .selectedOptions(
            entity.getSelectedOptions() != null
                ? entity.getSelectedOptions().stream()
                    .map(productMapper::toProductOptionDomain)
                    .collect(Collectors.toList())
                : null)
        .build();
  }
}
