/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payroll.domain.Payroll;
import aros.services.rms.core.payroll.domain.PayrollStatus;
import aros.services.rms.core.payroll.domain.port.input.GetPayrollUseCase;
import aros.services.rms.core.payroll.domain.port.input.ListPayrollsUseCase;
import aros.services.rms.core.payroll.domain.port.input.RegisterPayrollUseCase;
import aros.services.rms.infraestructure.common.exception.GlobalExceptionHandler;
import aros.services.rms.infraestructure.image.storage.local.LocalResourceConfig;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Currency;
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

/** Web MVC tests for {@link PayrollController}. */
@WebMvcTest(
    value = PayrollController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = LocalResourceConfig.class))
@Import({PayrollControllerTest.TestSecurityConfig.class, GlobalExceptionHandler.class})
class PayrollControllerTest {

  private static final Currency COP = Currency.getInstance("COP");

  @Autowired private MockMvc mockMvc;

  @MockitoBean private RegisterPayrollUseCase registerPayrollUseCase;
  @MockitoBean private GetPayrollUseCase getPayrollUseCase;
  @MockitoBean private ListPayrollsUseCase listPayrollsUseCase;

  @MockitoBean
  private aros.services.rms.core.payroll.domain.port.input.UpdatePayrollUseCase
      updatePayrollUseCase;

  @MockitoBean
  private aros.services.rms.core.payroll.domain.port.input.DeletePayrollUseCase
      deletePayrollUseCase;

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
  // UC-01: createPayroll_returns201
  // ---------------------------------------------------------------------------

  @Test
  void createPayroll_returns201() throws Exception {
    Payroll created = buildPayroll(1L, PayrollStatus.PENDING, new BigDecimal("2550000"));

    when(registerPayrollUseCase.register(any())).thenReturn(created);

    String requestJson =
        """
        {
          "userId": 1,
          "year": 2026,
          "month": 7,
          "periodStart": "2026-07-01",
          "periodEnd": "2026-07-31",
          "baseSalary": 2500000.00,
          "bonuses": 200000.00,
          "deductions": 150000.00,
          "hoursWorked": 192.0
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/payroll")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.userId").value(1))
        .andExpect(jsonPath("$.status").value("PENDING"))
        .andExpect(jsonPath("$.netAmount").value(2550000.0));
  }

  // ---------------------------------------------------------------------------
  // UC-02: createPayroll_validatesRequest
  // ---------------------------------------------------------------------------

  @Test
  void createPayroll_validatesRequest() throws Exception {
    // Missing required fields (userId, baseSalary, etc.)
    String invalidRequestJson =
        """
        {
          "year": 2026,
          "month": 7
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/payroll")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequestJson))
        .andExpect(status().isBadRequest());
  }

  // ---------------------------------------------------------------------------
  // UC-03: getPayroll_returns200
  // ---------------------------------------------------------------------------

  @Test
  void getPayroll_returns200() throws Exception {
    Payroll payroll = buildPayroll(1L, PayrollStatus.PENDING, new BigDecimal("2550000"));

    when(getPayrollUseCase.findByUserAndPeriod(1L, 2026, 7))
        .thenReturn(java.util.Optional.of(payroll));

    mockMvc
        .perform(get("/api/v1/payroll/worker/1/2026/7").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.userId").value(1))
        .andExpect(jsonPath("$.status").value("PENDING"));
  }

  // ---------------------------------------------------------------------------
  // UC-04: listPayrolls_returns200
  // ---------------------------------------------------------------------------

  @Test
  void listPayrolls_returns200() throws Exception {
    Payroll p1 = buildPayroll(1L, PayrollStatus.PENDING, new BigDecimal("2550000"));
    Payroll p2 = buildPayroll(2L, PayrollStatus.ACCRUED, new BigDecimal("3000000"));

    when(listPayrollsUseCase.findAll()).thenReturn(List.of(p1, p2));

    mockMvc
        .perform(get("/api/v1/payroll").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[1].id").value(2));
  }

  // ---------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------

  private static Payroll buildPayroll(Long id, PayrollStatus status, BigDecimal netAmount) {
    return new Payroll(
        id,
        1L,
        YearMonth.of(2026, 7),
        LocalDate.of(2026, 7, 1),
        LocalDate.of(2026, 7, 31),
        new Money(new BigDecimal("2500000"), COP),
        new Money(new BigDecimal("200000"), COP),
        new Money(new BigDecimal("150000"), COP),
        new Money(netAmount, COP),
        new BigDecimal("192"),
        status,
        "Overtime included",
        10L,
        Instant.parse("2026-07-15T10:30:00Z"),
        Instant.parse("2026-07-15T12:00:00Z"));
  }
}
