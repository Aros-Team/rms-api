/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.api;

import aros.services.rms.core.payroll.domain.PayrollSettlement;
import aros.services.rms.core.payroll.domain.SettlementType;
import aros.services.rms.core.payroll.domain.port.input.ListSettlementsUseCase;
import aros.services.rms.core.payroll.domain.port.input.RegisterSettlementUseCase;
import aros.services.rms.infraestructure.payroll.api.dto.PayrollSettlementRequest;
import aros.services.rms.infraestructure.payroll.api.dto.PayrollSettlementResponse;
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
public class PayrollSettlementController {

  private final RegisterSettlementUseCase registerSettlement;
  private final ListSettlementsUseCase listSettlements;

  /**
   * Registers a new payroll settlement.
   *
   * @param request the settlement request
   * @return the created settlement
   */
  @Operation(
      summary = "Register a settlement",
      description = "Record a payment against a payroll",
      responses = {
        @ApiResponse(responseCode = "201", description = "Settlement registered successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid settlement (exceeds pending amount)"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
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
            request.amount(),
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
}
