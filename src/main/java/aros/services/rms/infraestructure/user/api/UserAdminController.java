/* (C) 2026 */

package aros.services.rms.infraestructure.user.api;

import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.port.input.GetAllUsersUseCase;
import aros.services.rms.core.user.port.input.UpdateUserAccessUseCase;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import aros.services.rms.infraestructure.share.security.JustAdminUser;
import aros.services.rms.infraestructure.user.api.dto.UserAreasUpdateRequest;
import aros.services.rms.infraestructure.user.api.dto.UserRoleUpdateRequest;
import aros.services.rms.infraestructure.user.api.dto.UserStatusUpdateRequest;
import aros.services.rms.infraestructure.user.api.dto.UserSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Admin-only REST controller for user and role management in settings. */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/users")
@Tag(name = "Settings users", description = "Admin user and role management")
@JustAdminUser
public class UserAdminController {

  private final GetAllUsersUseCase getAllUsersUseCase;
  private final UpdateUserAccessUseCase updateUserAccessUseCase;
  private final UserRepositoryPort userRepositoryPort;

  /**
   * Lists every non-deleted user, including administrators.
   *
   * @return the user summaries
   */
  @GetMapping
  @Operation(
      summary = "List users",
      description =
          "Returns every non-deleted user with role, status and assigned areas. Admin access only.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Admin access required")
      })
  public ResponseEntity<List<UserSummaryResponse>> list() {
    return ResponseEntity.ok(
        getAllUsersUseCase.getAll().stream().map(UserSummaryResponse::fromDomain).toList());
  }

  /**
   * Changes a user's role.
   *
   * @param id the target user id
   * @param request the new role
   * @param jwt the authenticated administrator
   * @return the updated user summary
   */
  @PatchMapping("/{id}/role")
  @Operation(
      summary = "Change user role",
      description =
          "Promotes or demotes a user. The last administrator cannot be demoted and an "
              + "administrator cannot demote themselves.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Role updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid role change"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Admin access required"),
        @ApiResponse(responseCode = "404", description = "User not found")
      })
  public ResponseEntity<UserSummaryResponse> changeRole(
      @Parameter(description = "User ID", example = "1001", required = true) @PathVariable Long id,
      @Valid @RequestBody UserRoleUpdateRequest request,
      @AuthenticationPrincipal Jwt jwt) {
    User updated = updateUserAccessUseCase.changeRole(id, request.toRole(), currentUserId(jwt));
    return ResponseEntity.ok(UserSummaryResponse.fromDomain(updated));
  }

  /**
   * Replaces a user's assigned areas.
   *
   * @param id the target user id
   * @param request the new area ids
   * @return the updated user summary
   */
  @PatchMapping("/{id}/areas")
  @Operation(
      summary = "Change user areas",
      description = "Replaces the assigned areas of a user. Admin access only.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Areas updated successfully"),
        @ApiResponse(responseCode = "400", description = "Unknown area id"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Admin access required"),
        @ApiResponse(responseCode = "404", description = "User not found")
      })
  public ResponseEntity<UserSummaryResponse> changeAreas(
      @Parameter(description = "User ID", example = "1001", required = true) @PathVariable Long id,
      @Valid @RequestBody UserAreasUpdateRequest request) {
    User updated = updateUserAccessUseCase.changeAreas(id, request.areaIds());
    return ResponseEntity.ok(UserSummaryResponse.fromDomain(updated));
  }

  /**
   * Activates or deactivates a user.
   *
   * @param id the target user id
   * @param request the new status
   * @param jwt the authenticated administrator
   * @return the updated user summary
   */
  @PatchMapping("/{id}/status")
  @Operation(
      summary = "Change user status",
      description =
          "Activates or deactivates a user. Administrators cannot deactivate themselves or the "
              + "last administrator.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status change"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Admin access required"),
        @ApiResponse(responseCode = "404", description = "User not found")
      })
  public ResponseEntity<UserSummaryResponse> changeStatus(
      @Parameter(description = "User ID", example = "1001", required = true) @PathVariable Long id,
      @Valid @RequestBody UserStatusUpdateRequest request,
      @AuthenticationPrincipal Jwt jwt) {
    User updated = updateUserAccessUseCase.changeStatus(id, request.toStatus(), currentUserId(jwt));
    return ResponseEntity.ok(UserSummaryResponse.fromDomain(updated));
  }

  private Long currentUserId(Jwt jwt) {
    return userRepositoryPort
        .findByEmail(jwt.getSubject())
        .map(user -> user.getId().value())
        .orElse(null);
  }
}
