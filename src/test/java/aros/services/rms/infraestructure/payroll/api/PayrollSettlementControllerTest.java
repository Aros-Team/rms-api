/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.payroll.domain.PayrollSettlement;
import aros.services.rms.core.payroll.domain.SettlementType;
import aros.services.rms.core.payroll.domain.port.input.ListSettlementsUseCase;
import aros.services.rms.core.payroll.domain.port.input.RegisterSettlementUseCase;
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
 * Tests for {@link PayrollSettlementController}.
 *
 * <p><b>Feature:</b> Payroll settlement REST endpoint
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>should register settlement
 *   <li>should list settlements
 *   <li>should reject unauthenticated request
 *   <li>should return bad request when missing required fields
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
    value = PayrollSettlementController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = LocalResourceConfig.class))
@Import({PayrollSettlementControllerTest.TestSecurityConfig.class, GlobalExceptionHandler.class})
class PayrollSettlementControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private RegisterSettlementUseCase registerSettlement;
  @MockitoBean private ListSettlementsUseCase listSettlements;
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
  // UC-01: should_register_settlement
  // ---------------------------------------------------------------------------

  @Test
  void should_register_settlement() throws Exception {
    PayrollSettlement saved =
        new PayrollSettlement(
            1L,
            10L,
            1L,
            SettlementType.MONTHLY,
            LocalDate.of(2026, 8, 1),
            LocalDate.of(2026, 8, 31),
            new BigDecimal("2500000"),
            "August salary",
            Instant.parse("2026-08-31T12:00:00Z"),
            "system");

    when(registerSettlement.execute(any())).thenReturn(saved);

    String requestJson =
        """
        {
          "payrollId": 10,
          "userId": 1,
          "settlementType": "MONTHLY",
          "periodStart": "2026-08-01",
          "periodEnd": "2026-08-31",
          "amount": 2500000,
          "notes": "August salary"
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/payroll/settle")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.payrollId").value(10))
        .andExpect(jsonPath("$.userId").value(1))
        .andExpect(jsonPath("$.settlementType").value("MONTHLY"))
        .andExpect(jsonPath("$.amount").value(2500000))
        .andExpect(jsonPath("$.notes").value("August salary"));
  }

  // ---------------------------------------------------------------------------
  // UC-02: should_list_settlements
  // ---------------------------------------------------------------------------

  @Test
  void should_list_settlements() throws Exception {
    PayrollSettlement s1 =
        new PayrollSettlement(
            1L,
            10L,
            1L,
            SettlementType.MONTHLY,
            LocalDate.of(2026, 8, 1),
            LocalDate.of(2026, 8, 31),
            new BigDecimal("2500000"),
            "August",
            Instant.parse("2026-08-31T12:00:00Z"),
            "admin");

    PayrollSettlement s2 =
        new PayrollSettlement(
            2L,
            11L,
            1L,
            SettlementType.BIWEEKLY,
            LocalDate.of(2026, 8, 15),
            LocalDate.of(2026, 8, 31),
            new BigDecimal("1250000"),
            "Mid-month",
            Instant.parse("2026-08-31T12:00:00Z"),
            "admin");

    when(listSettlements.execute(eq(1L), any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(List.of(s1, s2));

    mockMvc
        .perform(
            get("/api/v1/payroll/settlements/1/2026/8")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].settlementType").value("MONTHLY"))
        .andExpect(jsonPath("$[1].id").value(2))
        .andExpect(jsonPath("$[1].settlementType").value("BIWEEKLY"));
  }

  // ---------------------------------------------------------------------------
  // UC-03: should_reject_unauthenticated_request
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_unauthenticated_request() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/payroll/settle").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isUnauthorized());

    verify(registerSettlement, never()).execute(any());
  }

  // ---------------------------------------------------------------------------
  // UC-04: should_return_bad_request_when_missing_required_fields
  // ---------------------------------------------------------------------------

  @Test
  void should_return_bad_request_when_missing_required_fields() throws Exception {
    String requestJson =
        """
        {
          "notes": "Missing required fields"
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/payroll/settle")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
        .andExpect(status().isBadRequest());
  }

  // ---------------------------------------------------------------------------
  // UC-05: should_list_settlements_empty
  // ---------------------------------------------------------------------------

  @Test
  void should_list_settlements_empty() throws Exception {
    when(listSettlements.execute(eq(99L), any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(List.of());

    mockMvc
        .perform(
            get("/api/v1/payroll/settlements/99/2026/8")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }
}
