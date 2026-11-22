/* (C) 2026 */

package aros.services.rms.infraestructure.cashshift.api;

import aros.services.rms.core.auth.port.output.CurrentUserPort;
import aros.services.rms.core.cashshift.application.dto.CloseCashShiftCommand;
import aros.services.rms.core.cashshift.application.dto.OpenCashShiftCommand;
import aros.services.rms.core.cashshift.domain.CashShift;
import aros.services.rms.core.cashshift.domain.CashShiftSummary;
import aros.services.rms.core.cashshift.port.input.CashShiftQueryUseCase;
import aros.services.rms.core.cashshift.port.input.CloseCashShiftUseCase;
import aros.services.rms.core.cashshift.port.input.OpenCashShiftUseCase;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import aros.services.rms.core.user.application.exception.UserNotFoundException;
import aros.services.rms.core.user.domain.UserWithAreas;
import aros.services.rms.infraestructure.cashshift.api.dto.CashShiftApiMapper;
import aros.services.rms.infraestructure.cashshift.api.dto.CashShiftResponse;
import aros.services.rms.infraestructure.cashshift.api.dto.CashShiftSummaryResponse;
import aros.services.rms.infraestructure.cashshift.api.dto.CloseCashShiftRequest;
import aros.services.rms.infraestructure.cashshift.api.dto.OpenCashShiftRequest;
import aros.services.rms.infraestructure.share.security.JustCashierOrAdmin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for the minimal cash-shift open/close and Z-summary. */
@RestController
@RequestMapping("/api/v1/cash-shifts")
@RequiredArgsConstructor
@Tag(name = "Cash shifts", description = "Open, close and reconcile the cash drawer")
@JustCashierOrAdmin
public class CashShiftController {

  private final OpenCashShiftUseCase openCashShiftUseCase;
  private final CloseCashShiftUseCase closeCashShiftUseCase;
  private final CashShiftQueryUseCase cashShiftQueryUseCase;
  private final CashShiftApiMapper cashShiftApiMapper;
  private final CurrentUserPort currentUserPort;
  private final CurrencyProvider currencyProvider;

  /**
   * Opens a cash shift.
   *
   * @param request the opening float
   * @return the opened shift
   */
  @Operation(
      summary = "Open a cash shift",
      description = "Requires a non-negative float. Only one shift may be open at a time.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Shift opened"),
        @ApiResponse(responseCode = "400", description = "Invalid float"),
        @ApiResponse(responseCode = "403", description = "Not a cashier"),
        @ApiResponse(responseCode = "409", description = "A shift is already open"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping("/open")
  public ResponseEntity<CashShiftResponse> open(@Valid @RequestBody OpenCashShiftRequest request) {
    CashShift shift =
        openCashShiftUseCase.open(
            new OpenCashShiftCommand(
                new Money(request.openingFloat(), currencyProvider.getCurrency()),
                currentUserId()));
    return ResponseEntity.status(HttpStatus.CREATED).body(cashShiftApiMapper.toResponse(shift));
  }

  /**
   * Closes a shift with the counted cash.
   *
   * @param id the shift id
   * @param request the counted cash and optional notes
   * @return the closed shift Z-summary
   */
  @Operation(
      summary = "Close a cash shift",
      description =
          "Snapshots expected cash, counted cash and the difference on the shift. "
              + "No further charge or void is allowed afterwards.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Shift closed"),
        @ApiResponse(responseCode = "400", description = "Invalid counted cash"),
        @ApiResponse(responseCode = "403", description = "Not a cashier"),
        @ApiResponse(responseCode = "404", description = "Shift not found"),
        @ApiResponse(responseCode = "409", description = "Shift already closed"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping("/{id}/close")
  public ResponseEntity<CashShiftSummaryResponse> close(
      @Parameter(description = "Shift ID", required = true) @PathVariable Long id,
      @Valid @RequestBody CloseCashShiftRequest request) {
    CashShiftSummary summary =
        closeCashShiftUseCase.close(
            new CloseCashShiftCommand(
                id,
                new Money(request.countedCash(), currencyProvider.getCurrency()),
                request.notes(),
                currentUserId()));
    return ResponseEntity.ok(cashShiftApiMapper.toSummary(summary));
  }

  /**
   * Returns the current open shift.
   *
   * @return the open shift, or 204 when none is open
   */
  @Operation(
      summary = "Get the current shift",
      description = "Returns 204 when no shift is open.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Open shift retrieved"),
        @ApiResponse(responseCode = "204", description = "No shift is open"),
        @ApiResponse(responseCode = "403", description = "Not a cashier"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/current")
  public ResponseEntity<CashShiftResponse> current() {
    return cashShiftQueryUseCase
        .findCurrent()
        .map(cashShiftApiMapper::toResponse)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.noContent().build());
  }

  /**
   * Returns a shift Z-summary.
   *
   * @param id the shift id
   * @return the shift summary
   */
  @Operation(
      summary = "Get a shift summary",
      description = "Returns expected vs counted cash and the method breakdown.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Shift summary retrieved"),
        @ApiResponse(responseCode = "403", description = "Not a cashier"),
        @ApiResponse(responseCode = "404", description = "Shift not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/{id}")
  public ResponseEntity<CashShiftSummaryResponse> findById(
      @Parameter(description = "Shift ID", required = true) @PathVariable Long id) {
    return ResponseEntity.ok(cashShiftApiMapper.toSummary(cashShiftQueryUseCase.findSummary(id)));
  }

  private Long currentUserId() {
    return currentUserPort
        .fetchCurrentUserInfo()
        .map(UserWithAreas::id)
        .map(userId -> userId.value())
        .orElseThrow(
            () -> new UserNotFoundException("The authenticated cashier could not be resolved"));
  }
}
