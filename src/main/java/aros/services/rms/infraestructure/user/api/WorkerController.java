/* (C) 2026 */

package aros.services.rms.infraestructure.user.api;

import aros.services.rms.core.auth.application.exception.UserNotFoundException;
import aros.services.rms.core.auth.port.input.AccountSetupUseCase;
import aros.services.rms.core.user.application.exception.UserAlreadyExistsException;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.port.input.CreateUserUseCase;
import aros.services.rms.core.user.port.input.DeleteUserUseCase;
import aros.services.rms.core.user.port.input.GetAllWorkersUseCase;
import aros.services.rms.core.user.port.input.GetSalaryHistoryUseCase;
import aros.services.rms.core.user.port.input.UpdateUserUseCase;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import aros.services.rms.infraestructure.share.security.JustAdminUser;
import aros.services.rms.infraestructure.user.api.dto.SalaryHistoryResponse;
import aros.services.rms.infraestructure.user.api.dto.UpdateUserRequest;
import aros.services.rms.infraestructure.user.api.dto.UserRegisterRequest;
import aros.services.rms.infraestructure.user.api.dto.UserRegisterResponse;
import aros.services.rms.infraestructure.user.api.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller exposing endpoints for worker management. */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/workers")
@Slf4j
@Tag(
    name = "Workers",
    description = "Operations for worker management: create, update, delete and view workers")
public class WorkerController {

  private final CreateUserUseCase createUserUseCase;
  private final GetAllWorkersUseCase getAllWorkersUseCase;
  private final UpdateUserUseCase updateUserUseCase;
  private final DeleteUserUseCase deleteUserUseCase;
  private final AccountSetupUseCase accountSetupUseCase;
  private final UserRepositoryPort userRepositoryPort;
  private final GetSalaryHistoryUseCase getSalaryHistoryUseCase;

  /**
   * Returns all workers in the system, optionally filtered by search term. Admin access only.
   *
   * @param search optional search term matching name or document (partial, case-insensitive)
   * @return list of workers
   */
  @GetMapping
  @JustAdminUser
  @Operation(
      tags = {"Workers"},
      summary = "Get all workers",
      description =
          "Returns a list of all workers in the system. "
              + "Optionally filters by search term matching name or document"
              + " (partial, case-insensitive). "
              + "Admin access only.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Workers retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Admin access required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<List<UserResponse>> getAll(
      @Parameter(
              description =
                  "Optional search term (partial, case-insensitive match on name or document)",
              example = "john")
          @RequestParam(required = false)
          String search) {
    List<User> workers;
    if (search != null && !search.isBlank()) {
      workers = getAllWorkersUseCase.getAllBySearch(search);
    } else {
      workers = getAllWorkersUseCase.getAll();
    }
    List<UserResponse> responses = workers.stream().map(UserResponse::fromDomain).toList();
    return ResponseEntity.ok(responses);
  }

  /**
   * Creates a new worker and sends a welcome email with temporary credentials. Admin only.
   *
   * @param request worker registration payload
   * @return the created worker with the temporary raw password
   */
  @PostMapping
  @JustAdminUser
  @Operation(
      tags = {"Workers"},
      summary = "Register new worker",
      description =
          "Creates a new worker in the system. A welcome email with temporary credentials is sent.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Worker created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Admin access required"),
        @ApiResponse(
            responseCode = "409",
            description = "Worker already exists with that document or email"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<UserRegisterResponse> register(
      @Valid @RequestBody UserRegisterRequest request) throws UserAlreadyExistsException {
    log.info("Admin is creating a new worker: document={}", request.document());
    var result = this.createUserUseCase.create(request.toCreateUserInfo());
    log.info("Worker created: id={}, status={}", result.user().getId(), result.user().getStatus());

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(UserRegisterResponse.fromDomain(result.user(), result.rawPassword()));
  }

  /**
   * Updates an existing worker. Admin access only.
   *
   * @param id worker ID to update
   * @param request update payload
   * @return the updated worker
   */
  @PutMapping("/{id}")
  @JustAdminUser
  @Operation(
      tags = {"Workers"},
      summary = "Update worker",
      description = "Updates an existing worker's data. Admin access only.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Worker updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Admin access required"),
        @ApiResponse(responseCode = "404", description = "Worker not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<UserResponse> update(
      @Parameter(description = "Worker ID", example = "1", required = true) @PathVariable Long id,
      @Valid @RequestBody UpdateUserRequest request)
      throws UserNotFoundException {
    log.info("Admin updating worker: id={}", id);
    var user = this.updateUserUseCase.update(id, request.toUpdateUserInfo());
    log.info("Worker updated successfully: id={}", id);
    return ResponseEntity.ok(UserResponse.fromDomain(user));
  }

  /**
   * Deletes a worker from the system. Admin access only.
   *
   * @param id worker ID to delete
   * @return empty response with 204 status
   */
  @DeleteMapping("/{id}")
  @JustAdminUser
  @Operation(
      tags = {"Workers"},
      summary = "Delete worker",
      description = "Deletes a worker from the system. Admin access only.",
      responses = {
        @ApiResponse(responseCode = "204", description = "Worker deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Admin access required"),
        @ApiResponse(responseCode = "404", description = "Worker not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Void> delete(
      @Parameter(description = "Worker ID", example = "1", required = true) @PathVariable Long id)
      throws UserNotFoundException {
    log.info("Admin deleting worker: id={}", id);
    this.deleteUserUseCase.delete(id);
    log.info("Worker deleted successfully: id={}", id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Invalidates existing setup tokens and sends a new account setup email. Admin only.
   *
   * @param id worker ID to resend the setup email to
   * @return empty response on success
   */
  @PostMapping("/{id}/retry-setup-email")
  @JustAdminUser
  @Operation(
      tags = {"Workers"},
      summary = "Resend setup email",
      description =
          "Invalidates existing tokens and sends a new account setup email. Admin access only.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Email resent successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Admin access required"),
        @ApiResponse(responseCode = "404", description = "Worker not found"),
        @ApiResponse(responseCode = "500", description = "Error sending email")
      })
  public ResponseEntity<Void> retrySetupEmail(
      @Parameter(description = "Worker ID", example = "1", required = true) @PathVariable Long id)
      throws UserNotFoundException {
    log.info("Admin retrying setup email for worker: id={}", id);
    User user =
        userRepositoryPort
            .findById(UserId.of(id))
            .orElseThrow(
                () ->
                    new aros.services.rms.core.user.application.exception.UserNotFoundException(
                        "Worker not found"));
    accountSetupUseCase.deleteExistingTokens(user.getId());
    accountSetupUseCase.requestSetupEmail(user.getEmail().value());
    log.info("Setup email resent successfully: id={}", id);
    return ResponseEntity.ok().build();
  }

  /** Retrieves the salary history for a worker. */
  @GetMapping("/{id}/salary-history")
  @JustAdminUser
  @Operation(
      tags = {"Workers"},
      summary = "Get worker salary history",
      description = "Returns the salary change history for a worker. Admin access only.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Salary history retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Admin access required"),
        @ApiResponse(responseCode = "404", description = "Worker not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<List<SalaryHistoryResponse>> getSalaryHistory(
      @Parameter(description = "Worker ID", example = "1", required = true) @PathVariable Long id) {
    log.info("Admin retrieving salary history for worker: id={}", id);
    List<SalaryHistoryResponse> history =
        getSalaryHistoryUseCase.getSalaryHistory(id).stream()
            .map(SalaryHistoryResponse::fromDomain)
            .toList();
    log.info("Salary history retrieved for worker: id={}, count={}", id, history.size());
    return ResponseEntity.ok(history);
  }
}
