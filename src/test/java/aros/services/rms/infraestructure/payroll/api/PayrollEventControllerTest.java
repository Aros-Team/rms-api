/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.payroll.domain.PayrollCalculationResult;
import aros.services.rms.core.payroll.domain.PayrollEvent;
import aros.services.rms.core.payroll.domain.PayrollEventType;
import aros.services.rms.core.payroll.domain.exception.PayrollEventNotFoundException;
import aros.services.rms.core.payroll.domain.port.input.CalculatePayrollEventUseCase;
import aros.services.rms.core.payroll.domain.port.input.DeletePayrollEventUseCase;
import aros.services.rms.core.payroll.domain.port.input.ListPayrollEventsUseCase;
import aros.services.rms.core.payroll.domain.port.input.RegisterPayrollEventUseCase;
import aros.services.rms.infraestructure.common.exception.GlobalExceptionHandler;
import aros.services.rms.infraestructure.image.storage.local.LocalResourceConfig;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests for {@link PayrollEventController}.
 *
 * <p><b>Feature:</b> Payroll event REST endpoint
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>should create event
 *   <li>should list events
 *   <li>should delete event
 *   <li>should reject unauthenticated request
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Uses MockMvc to simulate HTTP requests and verify responses
 *   <li>Mocks service-layer dependencies via Mockito
 * </ul>
 */
@WebMvcTest(
    value = PayrollEventController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = LocalResourceConfig.class))
@Import({PayrollEventControllerTest.TestSecurityConfig.class, GlobalExceptionHandler.class})
class PayrollEventControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private RegisterPayrollEventUseCase registerEvent;
  @MockitoBean private ListPayrollEventsUseCase listEvents;
  @MockitoBean private DeletePayrollEventUseCase deleteEvent;
  @MockitoBean private CalculatePayrollEventUseCase calculateEvent;
  @MockitoBean private JwtDecoder jwtDecoder;

  @org.springframework.boot.test.context.TestConfiguration
  static class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
      http.csrf(csrf -> csrf.disable())
          .sessionManagement(
              session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
          .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
      return http.build();
    }
  }

  // ---------------------------------------------------------------------------
  // UC-01: calculate_overtime_returns200WithMultiplier
  // ---------------------------------------------------------------------------

  @Test
  void calculate_overtime_returns200WithMultiplier() throws Exception {
    PayrollCalculationResult result =
        new PayrollCalculationResult(
            1L,
            "OVERTIME",
            new BigDecimal("15625"),
            new BigDecimal("1.5"),
            new BigDecimal("23437.50"),
            new BigDecimal("58593.75"));

    when(calculateEvent.calculate(eq(1L), eq("OVERTIME"), any(BigDecimal.class)))
        .thenReturn(result);

    String requestJson =
        """
        {
          "userId": 1,
          "eventType": "OVERTIME",
          "quantity": 2.5
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/payroll/events/calculate")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(1))
        .andExpect(jsonPath("$.eventType").value("OVERTIME"))
        .andExpect(jsonPath("$.hourlyRate").value(15625))
        .andExpect(jsonPath("$.multiplier").value(1.5))
        .andExpect(jsonPath("$.suggestedUnitRate").value(23437.50))
        .andExpect(jsonPath("$.suggestedAmount").value(58593.75));
  }

  // ---------------------------------------------------------------------------
  // UC-02: calculate_bonusPerformance_returnsNullMultiplier
  // ---------------------------------------------------------------------------

  @Test
  void calculate_bonusPerformance_returnsNullMultiplier() throws Exception {
    PayrollCalculationResult result =
        new PayrollCalculationResult(
            1L, "BONUS_PERFORMANCE", new BigDecimal("15625"), null, null, null);

    when(calculateEvent.calculate(eq(1L), eq("BONUS_PERFORMANCE"), any(BigDecimal.class)))
        .thenReturn(result);

    String requestJson =
        """
        {
          "userId": 1,
          "eventType": "BONUS_PERFORMANCE",
          "quantity": 1
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/payroll/events/calculate")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(1))
        .andExpect(jsonPath("$.eventType").value("BONUS_PERFORMANCE"))
        .andExpect(jsonPath("$.hourlyRate").value(15625))
        .andExpect(jsonPath("$.multiplier").doesNotExist())
        .andExpect(jsonPath("$.suggestedUnitRate").doesNotExist())
        .andExpect(jsonPath("$.suggestedAmount").doesNotExist());
  }

  // ---------------------------------------------------------------------------
  // UC-03: calculate_invalidEventType_returns500
  // ---------------------------------------------------------------------------

  @Test
  void calculate_invalidEventType_returns500() throws Exception {
    // Mock returns null by default for unstubbed calls; controller NPEs → 500
    String requestJson =
        """
        {
          "userId": 1,
          "eventType": "INVALID_TYPE",
          "quantity": 1
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/payroll/events/calculate")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
        .andExpect(status().isInternalServerError());
  }

  // ---------------------------------------------------------------------------
  // UC-04: calculate_missingFields_returns400
  // ---------------------------------------------------------------------------

  @Test
  void calculate_missingFields_returns400() throws Exception {
    String requestJson =
        """
        {
          "quantity": 1
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/payroll/events/calculate")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
        .andExpect(status().isBadRequest());
  }

  // ---------------------------------------------------------------------------
  // UC-05: should_create_event
  // ---------------------------------------------------------------------------

  @Test
  void should_create_event() throws Exception {
    PayrollEvent saved =
        new PayrollEvent(
            1L,
            1L,
            LocalDate.of(2026, 8, 1),
            PayrollEventType.OVERTIME,
            new BigDecimal("2.5"),
            new BigDecimal("23437.50"),
            new BigDecimal("58593.75"),
            "Weekend extra",
            Instant.parse("2026-08-01T10:00:00Z"),
            "system");

    when(registerEvent.execute(any())).thenReturn(saved);

    String requestJson =
        """
        {
          "userId": 1,
          "eventDate": "2026-08-01",
          "eventType": "OVERTIME",
          "quantity": 2.5,
          "unitRate": 23437.50,
          "notes": "Weekend extra"
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/payroll/events")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.userId").value(1))
        .andExpect(jsonPath("$.eventType").value("OVERTIME"))
        .andExpect(jsonPath("$.quantity").value(2.5))
        .andExpect(jsonPath("$.unitRate").value(23437.50))
        .andExpect(jsonPath("$.amount").value(58593.75))
        .andExpect(jsonPath("$.notes").value("Weekend extra"));
  }

  // ---------------------------------------------------------------------------
  // UC-06: should_list_events
  // ---------------------------------------------------------------------------

  @Test
  void should_list_events() throws Exception {
    PayrollEvent event1 =
        new PayrollEvent(
            1L,
            1L,
            LocalDate.of(2026, 8, 5),
            PayrollEventType.OVERTIME,
            new BigDecimal("2.5"),
            new BigDecimal("23437.50"),
            new BigDecimal("58593.75"),
            null,
            Instant.parse("2026-08-05T10:00:00Z"),
            "admin");

    PayrollEvent event2 =
        new PayrollEvent(
            2L,
            1L,
            LocalDate.of(2026, 8, 20),
            PayrollEventType.BONUS_ATTENDANCE,
            new BigDecimal("1"),
            new BigDecimal("7812.50"),
            new BigDecimal("7812.50"),
            "Perfect attendance",
            Instant.parse("2026-08-20T10:00:00Z"),
            "admin");

    when(listEvents.execute(eq(1L), any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(List.of(event1, event2));

    mockMvc
        .perform(
            get("/api/v1/payroll/events/1/2026/8")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].eventType").value("OVERTIME"))
        .andExpect(jsonPath("$[1].id").value(2))
        .andExpect(jsonPath("$[1].eventType").value("BONUS_ATTENDANCE"));
  }

  // ---------------------------------------------------------------------------
  // UC-07: should_delete_event
  // ---------------------------------------------------------------------------

  @Test
  void should_delete_event() throws Exception {
    doNothing().when(deleteEvent).execute(1L);

    mockMvc
        .perform(delete("/api/v1/payroll/events/1").with(jwt()))
        .andExpect(status().isNoContent());

    verify(deleteEvent).execute(1L);
  }

  // ---------------------------------------------------------------------------
  // UC-08: should_reject_unauthenticated_request
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_unauthenticated_request() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/payroll/events").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isUnauthorized());

    verify(registerEvent, never()).execute(any());
  }

  // ---------------------------------------------------------------------------
  // UC-09: should_return_not_found_when_event_not_found_on_delete
  // ---------------------------------------------------------------------------

  @Test
  void should_return_not_found_when_event_not_found_on_delete() throws Exception {
    doThrow(new PayrollEventNotFoundException(99L)).when(deleteEvent).execute(99L);

    mockMvc
        .perform(delete("/api/v1/payroll/events/99").with(jwt()))
        .andExpect(status().isNotFound());
  }

  // ---------------------------------------------------------------------------
  // UC-10: should_create_event_with_missing_required_fields_returns400
  // ---------------------------------------------------------------------------

  @Test
  void should_create_event_with_missing_required_fields_returns400() throws Exception {
    String requestJson =
        """
        {
          "notes": "Missing required fields"
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/payroll/events")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
        .andExpect(status().isBadRequest());
  }

  // ---------------------------------------------------------------------------
  // UC-11: should_list_events_empty_list
  // ---------------------------------------------------------------------------

  @Test
  void should_list_events_empty_list() throws Exception {
    when(listEvents.execute(eq(99L), any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(List.of());

    mockMvc
        .perform(
            get("/api/v1/payroll/events/99/2026/8")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }
}
