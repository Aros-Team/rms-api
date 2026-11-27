/* (C) 2026 */

package aros.services.rms.infraestructure.order.persistence.jpa;

import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderQueryResult;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.core.order.port.output.OrderRepositoryPort;
import aros.services.rms.infraestructure.order.persistence.OrderDetail;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/** Adapter for order persistence. */
@Component
@RequiredArgsConstructor
@Transactional
public class OrderPersistenceAdapter implements OrderRepositoryPort {

  private final OrderRepository orderRepository;
  private final OrderMapper orderMapper;

  @Override
  public Order save(Order order) {
    if (order.getId() != null) {
      Optional<aros.services.rms.infraestructure.order.persistence.Order> existingOpt =
          orderRepository.findById(order.getId());

      if (existingOpt.isPresent()) {
        aros.services.rms.infraestructure.order.persistence.Order existing = existingOpt.get();
        aros.services.rms.infraestructure.order.persistence.Order mapped =
            orderMapper.toEntity(order);

        existing.setDate(mapped.getDate());
        existing.setStatus(mapped.getStatus());
        existing.setTable(mapped.getTable());
        existing.setCloseTime(mapped.getCloseTime());
        existing.setPreparedAt(mapped.getPreparedAt());
        existing.setDeliveredAt(mapped.getDeliveredAt());

        syncDetails(existing, mapped);

        aros.services.rms.infraestructure.order.persistence.Order savedEntity =
            orderRepository.save(existing);
        return orderMapper.toDomain(savedEntity);
      }
    }

    aros.services.rms.infraestructure.order.persistence.Order orderEntity =
        orderMapper.toEntity(order);
    aros.services.rms.infraestructure.order.persistence.Order savedEntity =
        orderRepository.save(orderEntity);
    return orderMapper.toDomain(savedEntity);
  }

  /**
   * Applies the mapped detail rows onto the managed order without deleting the existing rows.
   *
   * <p>Clearing {@code existing.getDetails()} and re-adding rows with the same ids made Hibernate
   * delete details that still had {@code order_detail_options} children, so any update (marking a
   * line ready, delivering an order with choices) failed with a foreign-key violation.
   */
  private void syncDetails(
      aros.services.rms.infraestructure.order.persistence.Order existing,
      aros.services.rms.infraestructure.order.persistence.Order mapped) {
    List<OrderDetail> mappedDetails = mapped.getDetails() == null ? List.of() : mapped.getDetails();
    Map<Long, OrderDetail> currentById =
        existing.getDetails().stream()
            .filter(detail -> detail != null && detail.getId() != null)
            .collect(Collectors.toMap(OrderDetail::getId, Function.identity()));
    Set<Long> mappedIds =
        mappedDetails.stream()
            .map(OrderDetail::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(HashSet::new));

    existing
        .getDetails()
        .removeIf(
            detail ->
                detail != null && detail.getId() != null && !mappedIds.contains(detail.getId()));

    for (OrderDetail mappedDetail : mappedDetails) {
      if (mappedDetail == null) {
        continue;
      }
      OrderDetail current =
          mappedDetail.getId() == null ? null : currentById.get(mappedDetail.getId());
      if (current == null) {
        mappedDetail.setOrder(existing);
        existing.getDetails().add(mappedDetail);
        continue;
      }
      current.setProduct(mappedDetail.getProduct());
      current.setUnitPrice(mappedDetail.getUnitPrice());
      current.setInstructions(mappedDetail.getInstructions());
      current.setReady(mappedDetail.isReady());
      current.setReadyAt(mappedDetail.getReadyAt());
      current.setPreparationStartedAt(mappedDetail.getPreparationStartedAt());
      current.setDelivered(mappedDetail.isDelivered());
      current.setDeliveredAt(mappedDetail.getDeliveredAt());
      // Do not rebuild the selected-option rows here. They carry a composite
      // @MapsId key and Hibernate's orphan-removal of the old row fails when the
      // detail is updated (delivery/ready). Options only change while editing an
      // order, before any of these transitions.
    }
  }

  @Override
  public Optional<Order> findById(Long id) {
    return orderRepository.findById(id).map(orderMapper::toDomain);
  }

  @Override
  public Optional<Order> findFirstByStatusOrderByDateAsc(OrderStatus status) {
    aros.services.rms.infraestructure.order.persistence.OrderStatus infraStatus =
        aros.services.rms.infraestructure.order.persistence.OrderStatus.valueOf(status.name());
    return orderRepository.findFirstByStatusOrderByDateAsc(infraStatus).map(orderMapper::toDomain);
  }

  @Override
  public List<Order> findAll() {
    return orderRepository.findAll().stream()
        .map(orderMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<Order> findByStatus(OrderStatus status) {
    aros.services.rms.infraestructure.order.persistence.OrderStatus infraStatus =
        aros.services.rms.infraestructure.order.persistence.OrderStatus.valueOf(status.name());
    return orderRepository.findByStatus(infraStatus).stream()
        .map(orderMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<Order> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
    return orderRepository.findByDateBetween(startDate, endDate).stream()
        .map(orderMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<Order> findByStatusAndDateBetween(
      OrderStatus status, LocalDateTime startDate, LocalDateTime endDate) {
    aros.services.rms.infraestructure.order.persistence.OrderStatus infraStatus =
        aros.services.rms.infraestructure.order.persistence.OrderStatus.valueOf(status.name());
    return orderRepository.findByStatusAndDateBetween(infraStatus, startDate, endDate).stream()
        .map(orderMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public OrderQueryResult findOrdersPage(
      List<OrderStatus> statuses,
      LocalDateTime startDate,
      LocalDateTime endDate,
      Long areaId,
      int page,
      int size,
      String sort) {
    Sort springSort = parseSort(sort);
    PageRequest pageable = PageRequest.of(page, size, springSort);

    List<aros.services.rms.infraestructure.order.persistence.OrderStatus> infraStatuses =
        statuses == null || statuses.isEmpty()
            ? null
            : statuses.stream()
                .map(
                    s ->
                        aros.services.rms.infraestructure.order.persistence.OrderStatus.valueOf(
                            s.name()))
                .collect(Collectors.toList());
    LocalDateTime normalizedStartDate = startDate != null && endDate != null ? startDate : null;
    LocalDateTime normalizedEndDate = startDate != null && endDate != null ? endDate : null;

    Page<aros.services.rms.infraestructure.order.persistence.Order> result =
        orderRepository.findByFilters(
            infraStatuses, null, normalizedStartDate, normalizedEndDate, areaId, pageable);

    List<Order> domains =
        result.getContent().stream().map(orderMapper::toDomain).collect(Collectors.toList());

    return new OrderQueryResult(domains, result.getTotalElements(), page, size);
  }

  @Override
  public OrderQueryResult findOrdersPage(
      List<OrderStatus> statuses,
      String search,
      LocalDateTime startDate,
      LocalDateTime endDate,
      Long areaId,
      int page,
      int size,
      String sort) {
    Sort springSort = parseSort(sort);
    PageRequest pageable = PageRequest.of(page, size, springSort);

    List<aros.services.rms.infraestructure.order.persistence.OrderStatus> infraStatuses =
        statuses == null || statuses.isEmpty()
            ? null
            : statuses.stream()
                .map(
                    s ->
                        aros.services.rms.infraestructure.order.persistence.OrderStatus.valueOf(
                            s.name()))
                .collect(Collectors.toList());
    String normalizedSearch = search == null || search.isBlank() ? null : search.trim();
    LocalDateTime normalizedStartDate = startDate != null && endDate != null ? startDate : null;
    LocalDateTime normalizedEndDate = startDate != null && endDate != null ? endDate : null;

    Page<aros.services.rms.infraestructure.order.persistence.Order> result =
        orderRepository.findByFilters(
            infraStatuses,
            normalizedSearch,
            normalizedStartDate,
            normalizedEndDate,
            areaId,
            pageable);

    List<Order> domains =
        result.getContent().stream().map(orderMapper::toDomain).collect(Collectors.toList());

    return new OrderQueryResult(domains, result.getTotalElements(), page, size);
  }

  /** Whitelisted sort fields; anything else is rejected with 400 instead of a 500. */
  private static final java.util.Map<String, String> SORTABLE_FIELDS =
      java.util.Map.of(
          "id", "id",
          "date", "date",
          "status", "status",
          "tableId", "table.id",
          "partySize", "partySize",
          "openTime", "openTime",
          "closeTime", "closeTime");

  private static Sort parseSort(String sort) {
    if (sort == null || sort.isBlank()) {
      return Sort.by(Sort.Direction.DESC, "date");
    }
    String[] parts = sort.split(",");
    String requested = "date";
    Sort.Direction direction = Sort.Direction.DESC;
    if (parts.length >= 1 && !parts[0].isBlank()) {
      requested = parts[0].trim();
    }
    String field = SORTABLE_FIELDS.get(requested);
    if (field == null) {
      throw new IllegalArgumentException(
          "Campo de ordenamiento no soportado '"
              + requested
              + "'. Allowed: "
              + SORTABLE_FIELDS.keySet());
    }
    if (parts.length >= 2 && !parts[1].isBlank()) {
      try {
        direction = Sort.Direction.fromString(parts[1].trim());
      } catch (IllegalArgumentException e) {
        direction = Sort.Direction.DESC;
      }
    }
    return Sort.by(direction, field);
  }
}
