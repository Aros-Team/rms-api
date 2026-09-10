/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.api;

import aros.services.rms.core.payroll.domain.PayrollCalculationResult;
import aros.services.rms.core.payroll.domain.PayrollEvent;
import aros.services.rms.core.payroll.domain.PayrollEventType;
import aros.services.rms.core.payroll.domain.port.input.CalculatePayrollEventUseCase;
import aros.services.rms.core.payroll.domain.port.input.DeletePayrollEventUseCase;
import aros.services.rms.core.payroll.domain.port.input.ListPayrollEventsUseCase;
import aros.services.rms.core.payroll.domain.port.input.RegisterPayrollEventUseCase;
import aros.services.rms.infraestructure.payroll.api.dto.PayrollCalculateRequest;
import aros.services.rms.infraestructure.payroll.api.dto.PayrollCalculateResponse;
import aros.services.rms.infraestructure.payroll.api.dto.PayrollEventRequest;
import aros.services.rms.infraestructure.payroll.api.dto.PayrollEventResponse;
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

/** REST controller for payroll event management. */
@RestController
@RequestMapping("/api/v1/payroll/events")
@RequiredArgsConstructor
@Tag(name = "Payroll Events", description = "Manage payroll events (overtime, bonuses, deductions)")
public class PayrollEventController {

  private final RegisterPayrollEventUseCase registerEvent;
  private final ListPayrollEventsUseCase listEvents;
  private final DeletePayrollEventUseCase deleteEvent;
  private final CalculatePayrollEventUseCase calculateEvent;

  /**
   * Registers a new payroll event.
   *
   * @param request the payroll event request
   * @return the created event
   */
  @Operation(
      summary = "Register a payroll event",
      description = "Register a new payroll event and trigger aggregation",
      responses = {
        @ApiResponse(responseCode = "201", description = "Event registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<PayrollEventResponse> create(
      @Valid @RequestBody PayrollEventRequest request) {
    PayrollEvent event =
        PayrollEvent.create(
            request.userId(),
            request.eventDate(),
            PayrollEventType.valueOf(request.eventType()),
            request.quantity(),
            request.unitRate(),
            request.notes(),
            "system");
    PayrollEvent saved = registerEvent.execute(event);
    return new ResponseEntity<>(PayrollEventResponse.fromDomain(saved), HttpStatus.CREATED);
  }

  /**
   * Lists payroll events for a user in a specific year/month.
   *
   * @param userId the user id
   * @param year the year
   * @param month the month
   * @return list of matching events
   */
  @Operation(
      summary = "List events for period",
      description = "List all events for a user in a specific year/month",
      responses = {
        @ApiResponse(responseCode = "200", description = "Events retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/{userId}/{year}/{month}")
  public ResponseEntity<List<PayrollEventResponse>> list(
      @Parameter(description = "User ID", example = "1", required = true) @PathVariable Long userId,
      @Parameter(description = "Year", example = "2026", required = true) @PathVariable
          Integer year,
      @Parameter(description = "Month (1-12)", example = "8", required = true) @PathVariable
          Integer month) {
    LocalDate startDate = LocalDate.of(year, month, 1);
    LocalDate endDate = startDate.plusMonths(1).minusDays(1);
    List<PayrollEvent> events = listEvents.execute(userId, startDate, endDate);
    return ResponseEntity.ok(events.stream().map(PayrollEventResponse::fromDomain).toList());
  }

  /**
   * Deletes a payroll event by id.
   *
   * @param id the event id
   * @return no content
   */
  @Operation(
      summary = "Delete a payroll event",
      description = "Delete an event and re-aggregate payroll",
      responses = {
        @ApiResponse(responseCode = "204", description = "Event deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Event not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public ResponseEntity<Void> delete(
      @Parameter(description = "Event ID", example = "1", required = true) @PathVariable Long id) {
    deleteEvent.execute(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Calculates the suggested unit rate for a payroll event.
   *
   * @param request the calculation request
   * @return the suggested rate and amount
   */
  @Operation(
      summary = "Calculate suggested rate",
      description =
          "Calculate suggested unitRate based on user's hourly_rate and event type multiplier",
      responses = {
        @ApiResponse(responseCode = "200", description = "Calculation completed"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping("/calculate")
  public ResponseEntity<PayrollCalculateResponse> calculate(
      @Valid @RequestBody PayrollCalculateRequest request) {
    PayrollCalculationResult result =
        calculateEvent.calculate(request.userId(), request.eventType(), request.quantity());

    return ResponseEntity.ok(
        new PayrollCalculateResponse(
            result.userId(),
            result.eventType(),
            result.hourlyRate(),
            result.multiplier(),
            result.suggestedUnitRate(),
            result.suggestedAmount()));
  }
}
