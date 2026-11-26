/* (C) 2026 */

package aros.services.rms.core.notification.infrastructure.rest;

import aros.services.rms.core.notification.domain.NotificationPage;
import aros.services.rms.core.notification.domain.port.input.NotificationUseCase;
import aros.services.rms.core.user.application.exception.UserNotFoundException;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import aros.services.rms.infraestructure.share.security.JustAdminUser;
import aros.services.rms.infraestructure.share.security.JustWorkerOrAdmin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for the persistent notification feeds. */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Persistent operational notifications")
public class NotificationController {

  private final NotificationUseCase notificationUseCase;
  private final UserRepositoryPort userRepositoryPort;

  /**
   * Returns a page of global (admin) notifications, newest first.
   *
   * @param unreadOnly when true, only unread notifications are returned
   * @param limit page size (max 100)
   * @param offset zero-based offset
   * @return the page plus the current unread total
   */
  @GetMapping
  @JustAdminUser
  @Operation(
      summary = "List notifications",
      description = "Paged global feed ordered by creation time, newest first.")
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
   * Marks one global notification as read. Idempotent.
   *
   * @param id notification id
   * @return 204 No Content
   */
  @PatchMapping("/{id}/read")
  @JustAdminUser
  @Operation(summary = "Mark a notification as read", description = "Idempotent read transition.")
  @ApiResponse(responseCode = "204", description = "Notification marked as read")
  @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
  @ApiResponse(responseCode = "403", description = "Authenticated user lacks ROLE_ADMIN")
  public ResponseEntity<Void> markRead(@PathVariable Long id) {
    notificationUseCase.markRead(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Marks every unread global notification as read.
   *
   * @return 204 No Content
   */
  @PostMapping("/read-all")
  @JustAdminUser
  @Operation(summary = "Mark all notifications as read")
  @ApiResponse(responseCode = "204", description = "All notifications marked as read")
  @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
  @ApiResponse(responseCode = "403", description = "Authenticated user lacks ROLE_ADMIN")
  public ResponseEntity<Void> markAllRead() {
    notificationUseCase.markAllRead();
    return ResponseEntity.noContent().build();
  }

  /**
   * Deletes the whole global feed.
   *
   * @return 204 No Content
   */
  @DeleteMapping
  @JustAdminUser
  @Operation(summary = "Clear all notifications")
  @ApiResponse(responseCode = "204", description = "Feed cleared")
  @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
  @ApiResponse(responseCode = "403", description = "Authenticated user lacks ROLE_ADMIN")
  public ResponseEntity<Void> clear() {
    notificationUseCase.clear();
    return ResponseEntity.noContent().build();
  }

  /**
   * Returns the authenticated worker's targeted notifications.
   *
   * @param unreadOnly when true, only unread notifications are returned
   * @param limit page size (max 100)
   * @param offset zero-based offset
   * @param jwt the authenticated principal
   * @return the page plus the worker's unread total
   */
  @GetMapping("/me")
  @JustWorkerOrAdmin
  @Operation(
      summary = "List my notifications",
      description = "Paged feed of notifications targeted at the authenticated worker.")
  @ApiResponse(responseCode = "200", description = "Notification page retrieved")
  @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
  public NotificationPage listMine(
      @RequestParam(defaultValue = "false") boolean unreadOnly,
      @RequestParam(defaultValue = "50") int limit,
      @RequestParam(defaultValue = "0") int offset,
      @AuthenticationPrincipal Jwt jwt) {
    return notificationUseCase.listForUser(currentUserId(jwt), unreadOnly, limit, offset);
  }

  /**
   * Marks one of the authenticated worker's notifications as read.
   *
   * @param id notification id
   * @param jwt the authenticated principal
   * @return 204 No Content
   */
  @PatchMapping("/me/{id}/read")
  @JustWorkerOrAdmin
  @Operation(summary = "Mark my notification as read")
  @ApiResponse(responseCode = "204", description = "Notification marked as read")
  @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
  public ResponseEntity<Void> markMineRead(
      @PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
    notificationUseCase.markReadForUser(currentUserId(jwt), id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Marks every unread notification of the authenticated worker as read.
   *
   * @param jwt the authenticated principal
   * @return 204 No Content
   */
  @PostMapping("/me/read-all")
  @JustWorkerOrAdmin
  @Operation(summary = "Mark all my notifications as read")
  @ApiResponse(responseCode = "204", description = "All notifications marked as read")
  @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
  public ResponseEntity<Void> markAllMineRead(@AuthenticationPrincipal Jwt jwt) {
    notificationUseCase.markAllReadForUser(currentUserId(jwt));
    return ResponseEntity.noContent().build();
  }

  private Long currentUserId(Jwt jwt) {
    User user =
        userRepositoryPort
            .findByEmail(jwt.getSubject())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + jwt.getSubject()));
    return user.getId().value();
  }
}
