package aros.services.rms.core.common.metrics;

import java.time.LocalDateTime;

public interface BusinessMetricsPort {

  void recordLoginAttempt(String status);

  void recordPasswordReset(String type);

  void recordOrderCreated(boolean success);

  void recordOrderStatusTransition(String fromStatus, String toStatus);

  void recordOrderCancellation(String reason);

  void recordOrderDelivery();

  void recordKitchenLatency(
      long orderId, LocalDateTime orderCreatedAt, LocalDateTime preparationStartedAt);

  void recordNotificationLatency(
      long orderId, LocalDateTime preparationStartedAt, LocalDateTime readyAt);
}
