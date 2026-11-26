/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.api;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payroll.domain.Payroll;
import aros.services.rms.core.payroll.domain.PayrollStatus;
import aros.services.rms.core.payroll.domain.exception.PayrollNotFoundException;
import aros.services.rms.core.payroll.domain.port.input.DeletePayrollUseCase;
import aros.services.rms.core.payroll.domain.port.input.GetPayrollUseCase;
import aros.services.rms.core.payroll.domain.port.input.ListPayrollsUseCase;
import aros.services.rms.core.payroll.domain.port.input.RegisterPayrollUseCase;
import aros.services.rms.core.payroll.domain.port.input.UpdatePayrollUseCase;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import aros.services.rms.core.user.application.exception.UserNotFoundException;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import aros.services.rms.infraestructure.payroll.api.dto.PayrollRequest;
import aros.services.rms.infraestructure.payroll.api.dto.PayrollResponse;
import aros.services.rms.infraestructure.payroll.api.dto.PayrollUpdateRequest;
import aros.services.rms.infraestructure.share.security.JustAdminUser;
import aros.services.rms.infraestructure.share.security.JustWorkerOrAdmin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for payroll management. */
@RestController
@RequestMapping("/api/v1/payroll")
@RequiredArgsConstructor
@Tag(name = "Payroll", description = "Operations for managing employee payroll records")
@JustAdminUser
public class PayrollController {

  private final CurrencyProvider currencyProvider;
  private final RegisterPayrollUseCase registerPayrollUseCase;
  private final UpdatePayrollUseCase updatePayrollUseCase;
  private final GetPayrollUseCase getPayrollUseCase;
  private final ListPayrollsUseCase listPayrollsUseCase;
  private final DeletePayrollUseCase deletePayrollUseCase;
  private final UserRepositoryPort userRepositoryPort;

  /**
   * Creates a new payroll record.
   *
   * @param request the payroll request
   * @param jwt the authenticated admin, stored as the payroll registrar
   * @return the created payroll
   */
  @Operation(
      summary = "Create new payroll record",
      description =
          "Registers a new payroll for a user in a given period. "
              + "The netAmount is automatically calculated as baseSalary + bonuses - deductions.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Payroll created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(
            responseCode = "409",
            description = "Payroll already exists for user and period"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping
  public ResponseEntity<PayrollResponse> create(
      @Valid @RequestBody PayrollRequest request, @AuthenticationPrincipal Jwt jwt) {
    RegisterPayrollUseCase.RegisterPayrollCommand command =
        new RegisterPayrollUseCase.RegisterPayrollCommand(
            request.userId(),
            request.year(),
            request.month(),
            request.periodStart(),
            request.periodEnd(),
            new Money(request.baseSalary(), currencyProvider.getCurrency()),
            new Money(
                request.bonuses() != null ? request.bonuses() : BigDecimal.ZERO,
                currencyProvider.getCurrency()),
            new Money(
                request.deductions() != null ? request.deductions() : BigDecimal.ZERO,
                currencyProvider.getCurrency()),
            request.hoursWorked(),
            request.notes(),
            currentUserId(jwt));

    Payroll created = registerPayrollUseCase.register(command);
    return new ResponseEntity<>(PayrollResponse.fromDomain(created), HttpStatus.CREATED);
  }

  /**
   * Returns all payroll records.
   *
   * @return list of all payrolls
   */
  @Operation(
      summary = "Get all payroll records",
      description = "Returns a list of all payroll records in the system.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Payrolls retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping
  public ResponseEntity<List<PayrollResponse>> findAll() {
    List<Payroll> payrolls = listPayrollsUseCase.findAll();
    return ResponseEntity.ok(payrolls.stream().map(PayrollResponse::fromDomain).toList());
  }

  /**
   * Returns payroll records for a given period.
   *
   * @param year the year
   * @param month the month
   * @return list of matching payrolls
   */
  @Operation(
      summary = "Get payroll records by period",
      description = "Returns all payroll records for a specific year and month.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Payrolls retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid period parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/{year}/{month}")
  public ResponseEntity<List<PayrollResponse>> findByPeriod(
      @Parameter(description = "Period year", example = "2026", required = true) @PathVariable
          int year,
      @Parameter(description = "Period month (1-12)", example = "7", required = true) @PathVariable
          int month) {
    List<Payroll> payrolls = listPayrollsUseCase.findByPeriod(year, month);
    return ResponseEntity.ok(payrolls.stream().map(PayrollResponse::fromDomain).toList());
  }

  /**
   * Returns payroll history for a specific worker.
   *
   * @param userId the worker's user ID
   * @return list of payroll records for the worker
   */
  @Operation(
      summary = "Get payroll history for worker",
      description = "Returns all payroll records for a specific worker.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Payroll history retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/worker/{userId}")
  @JustWorkerOrAdmin
  public ResponseEntity<List<PayrollResponse>> findByUserId(
      @Parameter(description = "Worker user ID", example = "1", required = true) @PathVariable
          Long userId,
      @AuthenticationPrincipal Jwt jwt) {
    assertCanReadPayroll(userId, jwt);
    List<Payroll> payrolls = listPayrollsUseCase.findByUserId(userId);
    return ResponseEntity.ok(payrolls.stream().map(PayrollResponse::fromDomain).toList());
  }

  /**
   * Returns a specific payroll record for a worker in a given period.
   *
   * @param userId the worker's user ID
   * @param year the period year
   * @param month the period month
   * @return the payroll record if found
   */
  @Operation(
      summary = "Get payroll record for worker by period",
      description = "Returns a specific payroll record for a worker in a given year and month.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Payroll record retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Payroll record not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/worker/{userId}/{year}/{month}")
  @JustWorkerOrAdmin
  public ResponseEntity<PayrollResponse> findByUserAndPeriod(
      @Parameter(description = "Worker user ID", example = "1", required = true) @PathVariable
          Long userId,
      @Parameter(description = "Period year", example = "2026", required = true) @PathVariable
          int year,
      @Parameter(description = "Period month (1-12)", example = "7", required = true) @PathVariable
          int month,
      @AuthenticationPrincipal Jwt jwt) {
    assertCanReadPayroll(userId, jwt);
    Payroll payroll =
        getPayrollUseCase
            .findByUserAndPeriod(userId, year, month)
            .orElseThrow(
                () ->
                    new PayrollNotFoundException(
                        "Payroll not found for user "
                            + userId
                            + " en el periodo "
                            + year
                            + "-"
                            + month));
    return ResponseEntity.ok(PayrollResponse.fromDomain(payroll));
  }

  /**
   * Updates an existing payroll record.
   *
   * @param id the payroll ID
   * @param request the update request
   * @return the updated payroll
   */
  @Operation(
      summary = "Update payroll record",
      description =
          "Updates an existing payroll record. Only PENDING records can be modified. "
              + "Status transitions: PENDING → PAID/ACCRUED, ACCRUED → PAID, PAID → immutable.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Payroll updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Payroll not found"),
        @ApiResponse(responseCode = "409", description = "Payroll is immutable (already PAID)"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PatchMapping("/{id}")
  public ResponseEntity<PayrollResponse> update(
      @Parameter(description = "Payroll ID", example = "1", required = true) @PathVariable Long id,
      @Valid @RequestBody PayrollUpdateRequest request) {
    PayrollStatus status = null;
    if (request.status() != null) {
      status = PayrollStatus.valueOf(request.status());
    }

    UpdatePayrollUseCase.UpdatePayrollCommand command =
        new UpdatePayrollUseCase.UpdatePayrollCommand(
            request.baseSalary() != null
                ? new Money(request.baseSalary(), currencyProvider.getCurrency())
                : null,
            request.bonuses() != null
                ? new Money(request.bonuses(), currencyProvider.getCurrency())
                : null,
            request.deductions() != null
                ? new Money(request.deductions(), currencyProvider.getCurrency())
                : null,
            request.hoursWorked(),
            status,
            request.notes(),
            null);

    Payroll updated = updatePayrollUseCase.update(id, command);
    return ResponseEntity.ok(PayrollResponse.fromDomain(updated));
  }

  /**
   * Deletes a payroll record.
   *
   * @param id the payroll ID
   * @return no content
   */
  @Operation(
      summary = "Delete payroll record",
      description =
          "Deletes a payroll record. Only PENDING records can be deleted. "
              + "PAID and ACCRUED records are immutable.",
      responses = {
        @ApiResponse(responseCode = "204", description = "Payroll deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Payroll not found"),
        @ApiResponse(responseCode = "409", description = "Payroll is immutable (already PAID)"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
      @Parameter(description = "Payroll ID", example = "1", required = true) @PathVariable
          Long id) {
    deletePayrollUseCase.delete(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Rejects a non-admin caller reading payroll of a different worker.
   *
   * @param userId the worker whose payroll is requested
   * @param jwt the authenticated principal
   */
  private void assertCanReadPayroll(Long userId, Jwt jwt) {
    if ("ADMIN".equals(jwt.getClaimAsString("role"))) {
      return;
    }
    User self =
        userRepositoryPort
            .findByEmail(jwt.getSubject())
            .orElseThrow(
                () -> new AuthorizationDeniedException("Worker not found for the current session"));
    if (!self.getId().value().equals(userId)) {
      throw new AuthorizationDeniedException(
          "Los trabajadores solo pueden consultar su propia nómina");
    }
  }

  /**
   * Resolves the authenticated user's id from the JWT subject (the e-mail).
   *
   * @param jwt the authenticated principal
   * @return the user id stored as `registeredBy`
   * @throws UserNotFoundException if the subject does not match a user
   */
  private Long currentUserId(Jwt jwt) {
    return userRepositoryPort
        .findByEmail(jwt.getSubject())
        .map(User::getId)
        .map(UserId::value)
        .orElseThrow(
            () -> new UserNotFoundException("Authenticated user not found: " + jwt.getSubject()));
  }
}
