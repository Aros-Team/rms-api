/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.api;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payroll.domain.PayrollSettlement;
import aros.services.rms.core.payroll.domain.SettlementType;
import aros.services.rms.core.payroll.domain.port.input.ListSettlementsUseCase;
import aros.services.rms.core.payroll.domain.port.input.RegisterSettlementUseCase;
import aros.services.rms.core.payroll.domain.port.input.VoidSettlementUseCase;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import aros.services.rms.infraestructure.payroll.api.dto.PayrollSettlementRequest;
import aros.services.rms.infraestructure.payroll.api.dto.PayrollSettlementResponse;
import aros.services.rms.infraestructure.share.security.JustAdminUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for payroll settlement management. */
@RestController
@RequestMapping("/api/v1/payroll")
@RequiredArgsConstructor
@Tag(name = "Payroll Settlements", description = "Manage payroll settlements")
@JustAdminUser
public class PayrollSettlementController {

  private final RegisterSettlementUseCase registerSettlement;
  private final ListSettlementsUseCase listSettlements;
  private final VoidSettlementUseCase voidSettlement;
  private final CurrencyProvider currencyProvider;

  /**
   * Registers a new payroll settlement.
   *
   * @param request the settlement request
   * @return the created settlement
   */
  @Operation(
      summary = "Register a settlement",
      description =
          "Record a payment against a payroll. The amount cannot exceed the pending amount, "
              + "the settlement must belong to the payroll worker and period, and it cannot "
              + "overlap an existing settlement of the same payroll.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Settlement registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid settlement payload"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Payroll not found"),
        @ApiResponse(
            responseCode = "409",
            description = "Overpayment, overlapping period, worker mismatch or immutable payroll"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping("/settle")
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<PayrollSettlementResponse> settle(
      @Valid @RequestBody PayrollSettlementRequest request) {
    PayrollSettlement settlement =
        PayrollSettlement.create(
            request.payrollId(),
            request.userId(),
            SettlementType.valueOf(request.settlementType()),
            request.periodStart(),
            request.periodEnd(),
            new Money(request.amount(), currencyProvider.getCurrency()),
            request.notes(),
            "system");
    PayrollSettlement saved = registerSettlement.execute(settlement);
    return new ResponseEntity<>(PayrollSettlementResponse.fromDomain(saved), HttpStatus.CREATED);
  }

  /**
   * Lists settlements for a user in a specific year/month.
   *
   * @param userId the user id
   * @param year the year
   * @param month the month
   * @return list of matching settlements
   */
  @Operation(
      summary = "List settlements for period",
      description = "List all settlements for a user in a specific year/month",
      responses = {
        @ApiResponse(responseCode = "200", description = "Settlements retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/settlements/{userId}/{year}/{month}")
  public ResponseEntity<List<PayrollSettlementResponse>> list(
      @Parameter(description = "User ID", example = "1", required = true) @PathVariable Long userId,
      @Parameter(description = "Year", example = "2026", required = true) @PathVariable
          Integer year,
      @Parameter(description = "Month (1-12)", example = "8", required = true) @PathVariable
          Integer month) {
    LocalDate startDate = LocalDate.of(year, month, 1);
    LocalDate endDate = startDate.plusMonths(1).minusDays(1);
    List<PayrollSettlement> settlements = listSettlements.execute(userId, startDate, endDate);
    return ResponseEntity.ok(
        settlements.stream().map(PayrollSettlementResponse::fromDomain).toList());
  }

  /**
   * Voids a payroll settlement and restores the pending amount.
   *
   * @param id the settlement id
   * @return no content
   */
  @Operation(
      summary = "Void a settlement",
      description =
          "Remove a payment from a payroll. The paid and pending amounts are recomputed from "
              + "the settlements that remain. A PAID payroll is immutable.",
      responses = {
        @ApiResponse(responseCode = "204", description = "Settlement voided successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Settlement or payroll not found"),
        @ApiResponse(responseCode = "409", description = "Payroll is immutable (already PAID)"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @DeleteMapping("/settlements/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public ResponseEntity<Void> voidSettlement(
      @Parameter(description = "Settlement ID", example = "1", required = true) @PathVariable
          Long id) {
    voidSettlement.execute(id);
    return ResponseEntity.noContent().build();
  }
}
