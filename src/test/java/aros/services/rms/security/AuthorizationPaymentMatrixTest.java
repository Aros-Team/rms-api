/* (C) 2026 */

package aros.services.rms.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.auth.port.output.CurrentUserPort;
import aros.services.rms.core.cashshift.domain.CashShift;
import aros.services.rms.core.cashshift.domain.CashShiftStatus;
import aros.services.rms.core.cashshift.port.input.CashShiftQueryUseCase;
import aros.services.rms.core.cashshift.port.input.CloseCashShiftUseCase;
import aros.services.rms.core.cashshift.port.input.OpenCashShiftUseCase;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payment.domain.Payment;
import aros.services.rms.core.payment.domain.PaymentStatus;
import aros.services.rms.core.payment.domain.PendingOrderPage;
import aros.services.rms.core.payment.domain.RegisteredPayment;
import aros.services.rms.core.payment.port.input.PaymentQueryUseCase;
import aros.services.rms.core.payment.port.input.RegisterPaymentUseCase;
import aros.services.rms.core.payment.port.input.VoidPaymentUseCase;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import aros.services.rms.core.user.domain.UserEmail;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.domain.UserRole;
import aros.services.rms.core.user.domain.UserStatus;
import aros.services.rms.core.user.domain.UserWithAreas;
import aros.services.rms.infraestructure.cashshift.api.CashShiftController;
import aros.services.rms.infraestructure.cashshift.api.dto.CashShiftApiMapper;
import aros.services.rms.infraestructure.common.config.RestApiConfig;
import aros.services.rms.infraestructure.common.exception.GlobalExceptionHandler;
import aros.services.rms.infraestructure.image.storage.local.LocalResourceConfig;
import aros.services.rms.infraestructure.payment.api.PaymentController;
import aros.services.rms.infraestructure.payment.api.dto.PaymentApiMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests for {@link PaymentController} and {@link CashShiftController}: authorization matrix for the
 * payments and cash-shift endpoints.
 *
 * <p><b>Feature:</b> {@code @JustCashierOrAdmin} enforcement.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>ADMIN is allowed on every payments/cash-shift endpoint
 *   <li>A WORKER with the CASH area is allowed
 *   <li>A WORKER without the CASH area gets 403 problem+json
 *   <li>An anonymous caller gets 401
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Loads the two controllers in a WebMvc slice with method security
 *   <li>Attaches JWTs with different role/areaTypes claims per case
 * </ul>
 */
@WebMvcTest(
    controllers = {PaymentController.class, CashShiftController.class},
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = LocalResourceConfig.class))
@Import({
  AuthorizationPaymentMatrixTest.TestSecurityConfig.class,
  GlobalExceptionHandler.class,
  RestApiConfig.class,
  PaymentApiMapper.class,
  CashShiftApiMapper.class
})
class AuthorizationPaymentMatrixTest {

  private static final Currency COP = Currency.getInstance("COP");

  @Autowired private MockMvc mockMvc;

  @MockitoBean private RegisterPaymentUseCase registerPaymentUseCase;
  @MockitoBean private VoidPaymentUseCase voidPaymentUseCase;
  @MockitoBean private PaymentQueryUseCase paymentQueryUseCase;
  @MockitoBean private OpenCashShiftUseCase openCashShiftUseCase;
  @MockitoBean private CloseCashShiftUseCase closeCashShiftUseCase;
  @MockitoBean private CashShiftQueryUseCase cashShiftQueryUseCase;
  @MockitoBean private CurrentUserPort currentUserPort;
  @MockitoBean private CurrencyProvider currencyProvider;
  @MockitoBean private JwtDecoder jwtDecoder;

  @BeforeEach
  void setUp() {
    when(currencyProvider.getCurrency()).thenReturn(COP);
    when(currentUserPort.fetchCurrentUserInfo()).thenReturn(Optional.of(cashier()));
    when(paymentQueryUseCase.findPending(any(), any(), anyInt(), anyInt()))
        .thenReturn(new PendingOrderPage(List.of(), 0, 0, 20));
    when(paymentQueryUseCase.search(any()))
        .thenReturn(
            new aros.services.rms.core.payment.domain.PaymentQueryResult(List.of(), 0, 0, 20));
    when(registerPaymentUseCase.register(any()))
        .thenReturn(new RegisteredPayment(payment(), false));
    when(openCashShiftUseCase.open(any())).thenReturn(shift());
    when(cashShiftQueryUseCase.findCurrent()).thenReturn(Optional.of(shift()));
  }

  @Test
  void shouldAllowAdminToReadPendingOrders() throws Exception {
    mockMvc.perform(get("/api/v1/payments/pending").with(admin())).andExpect(status().isOk());
  }

  @Test
  void shouldAllowCashWorkerToReadPendingOrders() throws Exception {
    mockMvc
        .perform(get("/api/v1/payments/pending").with(worker("CASH")))
        .andExpect(status().isOk());
  }

  @Test
  void shouldDenyServiceWorkerOnPendingOrders() throws Exception {
    mockMvc
        .perform(get("/api/v1/payments/pending").with(worker("SERVICE")))
        .andExpect(status().isForbidden())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(403));
  }

  @Test
  void shouldDenyAnonymousOnPendingOrders() throws Exception {
    mockMvc.perform(get("/api/v1/payments/pending")).andExpect(status().isUnauthorized());
  }

  @Test
  void shouldAllowCashWorkerToCharge() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/payments")
                .header("Idempotency-Key", "key-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CHARGE_BODY)
                .with(worker("CASH")))
        .andExpect(status().isCreated());
  }

  @Test
  void shouldAllowAdminToCharge() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/payments")
                .header("Idempotency-Key", "key-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CHARGE_BODY)
                .with(admin()))
        .andExpect(status().isCreated());
  }

  @Test
  void shouldDenyServiceWorkerToCharge() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/payments")
                .header("Idempotency-Key", "key-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CHARGE_BODY)
                .with(worker("SERVICE")))
        .andExpect(status().isForbidden());
  }

  @Test
  void shouldDenyKitchenWorkerToReadThePaymentJournal() throws Exception {
    mockMvc
        .perform(get("/api/v1/payments").with(worker("KITCHEN")))
        .andExpect(status().isForbidden());
  }

  @Test
  void shouldAllowCashWorkerToReadTheCurrentShift() throws Exception {
    mockMvc
        .perform(get("/api/v1/cash-shifts/current").with(worker("CASH")))
        .andExpect(status().isOk());
  }

  @Test
  void shouldAllowAdminToOpenShift() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/cash-shifts/open")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"openingFloat\":\"200000.00\"}")
                .with(admin()))
        .andExpect(status().isCreated());
  }

  @Test
  void shouldDenyServiceWorkerToOpenShift() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/cash-shifts/open")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"openingFloat\":\"200000.00\"}")
                .with(worker("SERVICE")))
        .andExpect(status().isForbidden());
  }

  private static JwtRequestPostProcessor admin() {
    return jwt()
        .jwt(
            builder ->
                builder.subject("admin@test.com").claim("type", "access").claim("role", "ADMIN"));
  }

  private static JwtRequestPostProcessor worker(String... areaTypes) {
    return jwt()
        .jwt(
            builder ->
                builder
                    .subject("worker@test.com")
                    .claim("type", "access")
                    .claim("role", "WORKER")
                    .claim("areaTypes", List.of(areaTypes)));
  }

  private static UserWithAreas cashier() {
    return new UserWithAreas(
        UserId.of(1001L),
        "123",
        "Cashier",
        new UserEmail("cashier@test.com"),
        "encoded",
        "Address",
        "555-0100",
        UserRole.WORKER,
        UserStatus.ACTIVE,
        List.of());
  }

  private static Payment payment() {
    return Payment.builder()
        .id(9001L)
        .orderId(201767L)
        .shiftId(12L)
        .status(PaymentStatus.COMPLETED)
        .subtotal(money("58000.00"))
        .tip(money("0.00"))
        .total(money("58000.00"))
        .tendered(money("58000.00"))
        .change(money("0.00"))
        .tenders(List.of())
        .cashierId(1001L)
        .createdAt(LocalDateTime.now())
        .completedAt(LocalDateTime.now())
        .build();
  }

  private static CashShift shift() {
    return CashShift.builder()
        .id(12L)
        .status(CashShiftStatus.OPEN)
        .openingFloat(money("200000.00"))
        .openedBy(1001L)
        .openedAt(LocalDateTime.now())
        .build();
  }

  private static Money money(String amount) {
    return new Money(new BigDecimal(amount), COP);
  }

  private static final String CHARGE_BODY =
      """
      {
        "orderId": 201767,
        "tenders": [
          { "method": "CASH", "amount": "58000.00" }
        ]
      }
      """;

  @TestConfiguration
  @EnableMethodSecurity
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
}
