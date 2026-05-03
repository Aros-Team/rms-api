/* (C) 2026 */

package aros.services.rms.core.common.metrics;

import java.time.LocalDateTime;

/** Output port for recording business metrics and operational analytics. */
public interface BusinessMetricsPort {

  /**
   * Records a login attempt.
   *
   * @param status the login status (success, failure, tfa_required)
   */
  void recordLoginAttempt(String status);

  /**
   * Records a password reset event.
   *
   * @param type the password reset type (requested, completed)
   */
  void recordPasswordReset(String type);

  /**
   * Records an account setup event.
   *
   * @param type the account setup type (requested, completed)
   */
  void recordAccountSetup(String type);

  /**
   * Records an order creation event.
   *
   * @param success whether the order was created successfully
   */
  void recordOrderCreated(boolean success);

  /**
   * Records an order status transition.
   *
   * @param fromStatus the previous status
   * @param toStatus the new status
   */
  void recordOrderStatusTransition(String fromStatus, String toStatus);

  /**
   * Records an order cancellation.
   *
   * @param reason the cancellation reason
   */
  void recordOrderCancellation(String reason);

  /** Records an order delivery event. */
  void recordOrderDelivery();

  /**
   * Records kitchen latency (time from order creation to preparation start).
   *
   * @param orderId the order identifier
   * @param orderCreatedAt when the order was created
   * @param preparationStartedAt when preparation started
   */
  void recordKitchenLatency(
      long orderId, LocalDateTime orderCreatedAt, LocalDateTime preparationStartedAt);

  /**
   * Records notification latency (time from preparation start to ready).
   *
   * @param orderId the order identifier
   * @param preparationStartedAt when preparation started
   * @param readyAt when the order was marked ready
   */
  void recordNotificationLatency(
      long orderId, LocalDateTime preparationStartedAt, LocalDateTime readyAt);

  /** Records an account lockout event. */
  void recordAccountLockout();

  /** Records an insufficient stock event. */
  void recordInsufficientStock();

  /**
   * Records an inventory deduction event.
   *
   * @param success whether the deduction was successful
   */
  void recordInventoryDeduction(boolean success);

  /**
   * Records a purchase registration event.
   *
   * @param success whether the registration was successful
   */
  void recordPurchaseRegistered(boolean success);

  /** Records a fallback execution event. */
  void recordFallbackExecuted();

  /** Records a fallback failure event. */
  void recordFallbackFailed();

  /** Records an inventory reversion error. */
  void recordInventoryReversionError();

  /** Records a purchase order sync error. */
  void recordPurchaseOrderSyncError();
}
