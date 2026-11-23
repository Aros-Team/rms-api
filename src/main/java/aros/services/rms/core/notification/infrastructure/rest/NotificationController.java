/* (C) 2026 */

package aros.services.rms.core.notification.infrastructure.rest;

import aros.services.rms.core.notification.domain.NotificationPage;
import aros.services.rms.core.notification.domain.port.input.NotificationUseCase;
import aros.services.rms.infraestructure.share.security.JustAdminUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for the persistent global notification feed. */
@RestController
@RequestMapping("/api/v1/notifications")
@JustAdminUser
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Persistent operational notifications for the admin")
public class NotificationController {

  private final NotificationUseCase notificationUseCase;

  /**
   * Returns a page of notifications, newest first.
   *
   * @param unreadOnly when true, only unread notifications are returned
   * @param limit page size (max 100)
   * @param offset zero-based offset
   * @return the page plus the current unread total
   */
  @GetMapping
  @Operation(
      summary = "List notifications",
      description = "Paged feed ordered by creation time, newest first.")
  @ApiResponse(responseCode = "200", description = "Notification page retrieved")
  @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
  @ApiResponse(responseCode = "403", description = "Authenticated user lacks ROLE_ADMIN")
  public NotificationPage list(
      @RequestParam(defaultValue = "false") boolean unreadOnly,
      @RequestParam(defaultValue = "50") int limit,
      @RequestParam(defaultValue = "0") int offset) {
    return notificationUseCase.list(unreadOnly, limit, offset);
  }

  /**
   * Marks one notification as read. Idempotent.
   *
   * @param id notification id
   * @return 204 No Content
   */
  @PatchMapping("/{id}/read")
  @Operation(summary = "Mark a notification as read", description = "Idempotent read transition.")
  @ApiResponse(responseCode = "204", description = "Notification marked as read")
  @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
  @ApiResponse(responseCode = "403", description = "Authenticated user lacks ROLE_ADMIN")
  public ResponseEntity<Void> markRead(@PathVariable Long id) {
    notificationUseCase.markRead(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Marks every unread notification as read.
   *
   * @return 204 No Content
   */
  @PostMapping("/read-all")
  @Operation(summary = "Mark all notifications as read")
  @ApiResponse(responseCode = "204", description = "All notifications marked as read")
  @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
  @ApiResponse(responseCode = "403", description = "Authenticated user lacks ROLE_ADMIN")
  public ResponseEntity<Void> markAllRead() {
    notificationUseCase.markAllRead();
    return ResponseEntity.noContent().build();
  }

  /**
   * Deletes the whole feed.
   *
   * @return 204 No Content
   */
  @DeleteMapping
  @Operation(summary = "Clear all notifications")
  @ApiResponse(responseCode = "204", description = "Feed cleared")
  @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
  @ApiResponse(responseCode = "403", description = "Authenticated user lacks ROLE_ADMIN")
  public ResponseEntity<Void> clear() {
    notificationUseCase.clear();
    return ResponseEntity.noContent().build();
  }
}
