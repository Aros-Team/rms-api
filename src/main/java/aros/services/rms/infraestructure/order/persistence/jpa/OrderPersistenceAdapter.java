/* (C) 2026 */

package aros.services.rms.infraestructure.order.persistence.jpa;

import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderQueryResult;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.core.order.port.output.OrderRepositoryPort;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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

        // Clear existing details (triggers orphanRemoval) and add new ones
        existing.getDetails().clear();
        if (mapped.getDetails() != null) {
          mapped.getDetails().forEach(d -> d.setOrder(existing));
          existing.getDetails().addAll(mapped.getDetails());
        }

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
      int page,
      int size,
      String sort) {
    Sort springSort = parseSort(sort);
    PageRequest pageable = PageRequest.of(page, size, springSort);

    boolean hasStatuses = statuses != null && !statuses.isEmpty();
    boolean hasDates = startDate != null && endDate != null;

    Page<aros.services.rms.infraestructure.order.persistence.Order> result;
    if (hasStatuses && hasDates) {
      List<aros.services.rms.infraestructure.order.persistence.OrderStatus> infraStatuses =
          statuses.stream()
              .map(
                  s ->
                      aros.services.rms.infraestructure.order.persistence.OrderStatus.valueOf(
                          s.name()))
              .collect(Collectors.toList());
      result =
          orderRepository.findByStatusInAndDateBetween(infraStatuses, startDate, endDate, pageable);
    } else if (hasStatuses) {
      List<aros.services.rms.infraestructure.order.persistence.OrderStatus> infraStatuses =
          statuses.stream()
              .map(
                  s ->
                      aros.services.rms.infraestructure.order.persistence.OrderStatus.valueOf(
                          s.name()))
              .collect(Collectors.toList());
      result = orderRepository.findByStatusIn(infraStatuses, pageable);
    } else if (hasDates) {
      result = orderRepository.findByDateBetween(startDate, endDate, pageable);
    } else {
      result = orderRepository.findAll(pageable);
    }

    List<Order> domains =
        result.getContent().stream().map(orderMapper::toDomain).collect(Collectors.toList());

    return new OrderQueryResult(domains, result.getTotalElements(), page, size);
  }

  private static Sort parseSort(String sort) {
    if (sort == null || sort.isBlank()) {
      return Sort.by(Sort.Direction.DESC, "date");
    }
    String[] parts = sort.split(",");
    String field = "date";
    Sort.Direction direction = Sort.Direction.DESC;
    if (parts.length >= 1 && !parts[0].isBlank()) {
      field = parts[0].trim();
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
