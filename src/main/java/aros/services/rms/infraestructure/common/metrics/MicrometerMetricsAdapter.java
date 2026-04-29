package aros.services.rms.infraestructure.common.metrics;

import aros.services.rms.core.common.metrics.BusinessMetricsPort;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Adapter for Micrometer metrics. */
@Component
public class MicrometerMetricsAdapter implements BusinessMetricsPort {

  private static final String METRIC_PREFIX = "rms_";
  private static final Logger log = LoggerFactory.getLogger(MicrometerMetricsAdapter.class);

  private final MeterRegistry registry;

  private final Counter loginSuccessCounter;
  private final Counter loginFailureCounter;
  private final Counter loginTfaRequiredCounter;
  private final Counter passwordResetRequestedCounter;
  private final Counter passwordResetCompletedCounter;
  private final Counter accountSetupRequestedCounter;
  private final Counter accountSetupCompletedCounter;
  private final Counter orderCreatedSuccessCounter;
  private final Counter orderCreatedFailureCounter;
  private final Counter orderCancelledCounter;
  private final Counter orderDeliveredCounter;
  private final Timer kitchenLatencyTimer;
  private final Timer notificationLatencyTimer;
  private final Counter accountLockoutCounter;
  private final Counter insufficientStockCounter;
  private final Counter inventoryDeductionSuccessCounter;
  private final Counter inventoryDeductionFailureCounter;
  private final Counter purchaseRegisteredSuccessCounter;
  private final Counter purchaseRegisteredFailureCounter;
  private final Counter fallbackExecutedCounter;
  private final Counter fallbackFailedCounter;
  private final Counter inventoryReversionErrorCounter;
  private final Counter purchaseSyncErrorCounter;

  /**
   * Creates a new MicrometerMetricsAdapter.
   *
   * @param registry the meter registry
   */
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

    this.accountSetupRequestedCounter =
        Counter.builder(METRIC_PREFIX + "account_setup_total")
            .tag("type", "requested")
            .description("Account setup requests")
            .register(registry);

    this.accountSetupCompletedCounter =
        Counter.builder(METRIC_PREFIX + "account_setup_total")
            .tag("type", "completed")
            .description("Account setup completions")
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

    this.accountLockoutCounter =
        Counter.builder(METRIC_PREFIX + "auth_lockout_total")
            .description("Account lockouts due to failed login attempts")
            .register(registry);

    this.insufficientStockCounter =
        Counter.builder(METRIC_PREFIX + "inventory_insufficient_stock_total")
            .description("Insufficient stock exceptions")
            .register(registry);

    this.inventoryDeductionSuccessCounter =
        Counter.builder(METRIC_PREFIX + "inventory_deduction_total")
            .tag("status", "success")
            .description("Successful inventory deductions")
            .register(registry);

    this.inventoryDeductionFailureCounter =
        Counter.builder(METRIC_PREFIX + "inventory_deduction_total")
            .tag("status", "failure")
            .description("Failed inventory deductions")
            .register(registry);

    this.purchaseRegisteredSuccessCounter =
        Counter.builder(METRIC_PREFIX + "purchase_registered_total")
            .tag("status", "success")
            .description("Successful purchase registrations")
            .register(registry);

    this.purchaseRegisteredFailureCounter =
        Counter.builder(METRIC_PREFIX + "purchase_registered_total")
            .tag("status", "failure")
            .description("Failed purchase registrations")
            .register(registry);

    this.fallbackExecutedCounter =
        Counter.builder(METRIC_PREFIX + "inventory_fallback_total")
            .tag("status", "executed")
            .description("Inventory fallback executions (Cocina to Bodega)")
            .register(registry);

    this.fallbackFailedCounter =
        Counter.builder(METRIC_PREFIX + "inventory_fallback_total")
            .tag("status", "failed")
            .description("Failed inventory fallback executions")
            .register(registry);

    this.inventoryReversionErrorCounter =
        Counter.builder(METRIC_PREFIX + "inventory_reversion_errors_total")
            .description("Inventory reversions errors on order cancellation")
            .register(registry);

    this.purchaseSyncErrorCounter =
        Counter.builder(METRIC_PREFIX + "purchase_sync_errors_total")
            .description("Purchase order sync errors")
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
  public void recordAccountSetup(String type) {
    switch (type) {
      case "requested" -> accountSetupRequestedCounter.increment();
      case "completed" -> accountSetupCompletedCounter.increment();
    }
  }

  @Override
  public void recordOrderCreated(boolean success) {
    log.info("recordOrderCreated called with success={}", success);
    if (success) {
      log.info("Incrementing orderCreatedSuccessCounter");
      orderCreatedSuccessCounter.increment();
    } else {
      log.info("Incrementing orderCreatedFailureCounter");
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

  @Override
  public void recordAccountLockout() {
    accountLockoutCounter.increment();
  }

  @Override
  public void recordInsufficientStock() {
    insufficientStockCounter.increment();
  }

  @Override
  public void recordInventoryDeduction(boolean success) {
    if (success) {
      inventoryDeductionSuccessCounter.increment();
    } else {
      inventoryDeductionFailureCounter.increment();
    }
  }

  @Override
  public void recordPurchaseRegistered(boolean success) {
    if (success) {
      purchaseRegisteredSuccessCounter.increment();
    } else {
      purchaseRegisteredFailureCounter.increment();
    }
  }

  @Override
  public void recordFallbackExecuted() {
    fallbackExecutedCounter.increment();
  }

  @Override
  public void recordFallbackFailed() {
    fallbackFailedCounter.increment();
  }

  @Override
  public void recordInventoryReversionError() {
    inventoryReversionErrorCounter.increment();
  }

  @Override
  public void recordPurchaseOrderSyncError() {
    purchaseSyncErrorCounter.increment();
  }
}
