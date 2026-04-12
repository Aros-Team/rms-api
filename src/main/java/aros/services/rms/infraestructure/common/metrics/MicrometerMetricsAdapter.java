package aros.services.rms.infraestructure.common.metrics;

import aros.services.rms.core.common.metrics.BusinessMetricsPort;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class MicrometerMetricsAdapter implements BusinessMetricsPort {

  private static final String METRIC_PREFIX = "rms_";

  private final MeterRegistry registry;

  private final Counter loginSuccessCounter;
  private final Counter loginFailureCounter;
  private final Counter loginTfaRequiredCounter;
  private final Counter passwordResetRequestedCounter;
  private final Counter passwordResetCompletedCounter;
  private final Counter orderCreatedSuccessCounter;
  private final Counter orderCreatedFailureCounter;
  private final Counter orderCancelledCounter;
  private final Counter orderDeliveredCounter;
  private final Timer kitchenLatencyTimer;
  private final Timer notificationLatencyTimer;

  public MicrometerMetricsAdapter(MeterRegistry registry) {
    this.registry = registry;

    this.loginSuccessCounter =
        Counter.builder(METRIC_PREFIX + "auth_login_total")
            .tag("status", "success")
            .description("Successful login attempts")
            .register(registry);

    this.loginFailureCounter =
        Counter.builder(METRIC_PREFIX + "auth_login_total")
            .tag("status", "failure")
            .description("Failed login attempts")
            .register(registry);

    this.loginTfaRequiredCounter =
        Counter.builder(METRIC_PREFIX + "auth_login_total")
            .tag("status", "tfa_required")
            .description("Login attempts requiring 2FA")
            .register(registry);

    this.passwordResetRequestedCounter =
        Counter.builder(METRIC_PREFIX + "password_reset_total")
            .tag("type", "requested")
            .description("Password reset requests")
            .register(registry);

    this.passwordResetCompletedCounter =
        Counter.builder(METRIC_PREFIX + "password_reset_total")
            .tag("type", "completed")
            .description("Password reset completions")
            .register(registry);

    this.orderCreatedSuccessCounter =
        Counter.builder(METRIC_PREFIX + "order_created_total")
            .tag("status", "success")
            .description("Successfully created orders")
            .register(registry);

    this.orderCreatedFailureCounter =
        Counter.builder(METRIC_PREFIX + "order_created_total")
            .tag("status", "failure")
            .description("Failed order creation attempts")
            .register(registry);

    this.orderCancelledCounter =
        Counter.builder(METRIC_PREFIX + "order_cancelled_total")
            .description("Cancelled orders")
            .register(registry);

    this.orderDeliveredCounter =
        Counter.builder(METRIC_PREFIX + "order_delivered_total")
            .description("Delivered orders")
            .register(registry);

    this.kitchenLatencyTimer =
        Timer.builder(METRIC_PREFIX + "kitchen_latency_seconds")
            .description("Time from order creation to kitchen start")
            .register(registry);

    this.notificationLatencyTimer =
        Timer.builder(METRIC_PREFIX + "notification_latency_seconds")
            .description("Time from kitchen start to order ready")
            .register(registry);
  }

  @Override
  public void recordLoginAttempt(String status) {
    switch (status) {
      case "success" -> loginSuccessCounter.increment();
      case "failure" -> loginFailureCounter.increment();
      case "tfa_required" -> loginTfaRequiredCounter.increment();
    }
  }

  @Override
  public void recordPasswordReset(String type) {
    switch (type) {
      case "requested" -> passwordResetRequestedCounter.increment();
      case "completed" -> passwordResetCompletedCounter.increment();
    }
  }

  @Override
  public void recordOrderCreated(boolean success) {
    if (success) {
      orderCreatedSuccessCounter.increment();
    } else {
      orderCreatedFailureCounter.increment();
    }
  }

  @Override
  public void recordOrderStatusTransition(String fromStatus, String toStatus) {
    Counter.builder(METRIC_PREFIX + "order_status_transition_total")
        .tag("from", fromStatus)
        .tag("to", toStatus)
        .description("Order status transitions")
        .register(registry)
        .increment();
  }

  @Override
  public void recordOrderCancellation(String reason) {
    Counter.builder(METRIC_PREFIX + "order_cancelled_total")
        .tag("reason", reason)
        .description("Cancelled orders by reason")
        .register(registry)
        .increment();
  }

  @Override
  public void recordOrderDelivery() {
    orderDeliveredCounter.increment();
  }

  @Override
  public void recordKitchenLatency(
      long orderId, LocalDateTime orderCreatedAt, LocalDateTime preparationStartedAt) {
    Duration duration = Duration.between(orderCreatedAt, preparationStartedAt);
    kitchenLatencyTimer.record(duration);
  }

  @Override
  public void recordNotificationLatency(
      long orderId, LocalDateTime preparationStartedAt, LocalDateTime readyAt) {
    Duration duration = Duration.between(preparationStartedAt, readyAt);
    notificationLatencyTimer.record(duration);
  }
}
