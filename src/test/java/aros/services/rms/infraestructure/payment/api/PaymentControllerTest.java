/* (C) 2026 */

package aros.services.rms.infraestructure.payment.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.auth.port.output.CurrentUserPort;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payment.application.exception.InvalidPaymentException;
import aros.services.rms.core.payment.application.exception.OrderAlreadyPaidException;
import aros.services.rms.core.payment.domain.Payment;
import aros.services.rms.core.payment.domain.PaymentQueryResult;
import aros.services.rms.core.payment.domain.PaymentStatus;
import aros.services.rms.core.payment.domain.PaymentTender;
import aros.services.rms.core.payment.domain.PaymentTenderMethod;
import aros.services.rms.core.payment.domain.PendingOrder;
import aros.services.rms.core.payment.domain.PendingOrderPage;
import aros.services.rms.core.payment.domain.PendingPaymentStatus;
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
import aros.services.rms.testsupport.AbstractJwtIntegrationTest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Tests for {@link PaymentController}: charge, void and query payments over HTTP.
 *
 * <p><b>Feature:</b> Charge, void and query payments over HTTP.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>A new charge returns 201 inside the {@code data} envelope
 *   <li>An idempotent replay returns 200 with the same payment
 *   <li>Invalid data returns 400 problem+json; already paid returns 409 with the payment id
 *   <li>A worker without the CASH area gets 403; the pending board and journal are paged
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Loads the full context on H2 and mocks the use cases
 *   <li>Drives MockMvc requests with a JWT principal carrying role and areaTypes
 * </ul>
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = {
      "spring.flyway.enabled=false",
      "spring.jpa.hibernate.ddl-auto=create-drop",
      "spring.sql.init.mode=never",
      "spring.datasource.url=jdbc:h2:mem:paymentdb;DB_CLOSE_DELAY=-1;MODE=MYSQL",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.datasource.username=sa",
      "spring.datasource.password=sa",
      "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
      "app.admin.email=admin@test.local",
      "app.admin.dummy-email=test@test.local",
      "app.admin.password=TestPassword123!",
      "app.env=development"
    })
class PaymentControllerTest extends AbstractJwtIntegrationTest {

  private static final Currency COP = Currency.getInstance("COP");

  @Autowired private WebApplicationContext context;

  @MockitoBean private RegisterPaymentUseCase registerPaymentUseCase;
  @MockitoBean private VoidPaymentUseCase voidPaymentUseCase;
  @MockitoBean private PaymentQueryUseCase paymentQueryUseCase;
  @MockitoBean private CurrentUserPort currentUserPort;
  @MockitoBean private CurrencyProvider currencyProvider;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    authenticate(List.of("CASH"));
    when(currentUserPort.fetchCurrentUserInfo()).thenReturn(Optional.of(cashier()));
    when(currencyProvider.getCurrency()).thenReturn(COP);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldReturn201WithTheServerTotalWhenTheChargeSucceeds() throws Exception {
    when(registerPaymentUseCase.register(any()))
        .thenReturn(new RegisteredPayment(payment(9001L, PaymentStatus.COMPLETED), false));

    mockMvc
        .perform(
            post("/api/v1/payments")
                .header("Idempotency-Key", "key-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CHARGE_BODY))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id").value(9001))
        .andExpect(jsonPath("$.data.status").value("COMPLETED"))
        .andExpect(jsonPath("$.data.subtotal.amount").value("58000.00"))
        .andExpect(jsonPath("$.data.total.amount").value("63000.00"))
        .andExpect(jsonPath("$.data.change.amount").value("7000.00"))
        .andExpect(jsonPath("$.data.tenders[0].method").value("CARD"))
        .andExpect(jsonPath("$.data.tenders[1].method").value("CASH"));
  }

  @Test
  void shouldReturn200WhenTheChargeIsAnIdempotentReplay() throws Exception {
    when(registerPaymentUseCase.register(any()))
        .thenReturn(new RegisteredPayment(payment(9001L, PaymentStatus.COMPLETED), true));

    mockMvc
        .perform(
            post("/api/v1/payments")
                .header("Idempotency-Key", "key-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CHARGE_BODY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value(9001));
  }

  @Test
  void shouldReturn400ProblemWhenThePaymentIsInvalid() throws Exception {
    when(registerPaymentUseCase.register(any()))
        .thenThrow(new InvalidPaymentException("The tenders do not cover the total"));

    mockMvc
        .perform(
            post("/api/v1/payments")
                .header("Idempotency-Key", "key-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CHARGE_BODY))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.detail").value("The tenders do not cover the total"));
  }

  @Test
  void shouldReturn409WithThePaymentIdWhenTheOrderIsAlreadyPaid() throws Exception {
    when(registerPaymentUseCase.register(any()))
        .thenThrow(new OrderAlreadyPaidException(201767L, 9001L));

    mockMvc
        .perform(
            post("/api/v1/payments")
                .header("Idempotency-Key", "key-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CHARGE_BODY))
        .andExpect(status().isConflict())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.paymentId").value(9001));
  }

  @Test
  void shouldReturn403WhenTheWorkerHasNoCashArea() throws Exception {
    authenticate(List.of("SERVICE"));

    mockMvc
        .perform(
            post("/api/v1/payments")
                .header("Idempotency-Key", "key-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CHARGE_BODY))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
  }

  @Test
  void shouldReturnThePendingBoardPage() throws Exception {
    PendingOrder pending =
        new PendingOrder(
            201767L,
            3L,
            3,
            4,
            LocalDateTime.of(2026, 11, 21, 19, 58),
            money("58000.00"),
            PendingPaymentStatus.UNPAID);
    when(paymentQueryUseCase.findPending(any(), any(), anyInt(), anyInt()))
        .thenReturn(new PendingOrderPage(List.of(pending), 1, 0, 20));

    mockMvc
        .perform(get("/api/v1/payments/pending"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content[0].orderId").value(201767))
        .andExpect(jsonPath("$.data.content[0].paymentStatus").value("UNPAID"))
        .andExpect(jsonPath("$.data.page.totalElements").value(1))
        .andExpect(jsonPath("$.data.page.totalPages").value(1));
  }

  @Test
  void shouldReturnPaymentById() throws Exception {
    when(paymentQueryUseCase.findById(9001L)).thenReturn(payment(9001L, PaymentStatus.COMPLETED));

    mockMvc
        .perform(get("/api/v1/payments/9001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value(9001))
        .andExpect(jsonPath("$.data.receiptNumber").value("R-20261121-009001"));
  }

  @Test
  void shouldReturnThePaymentJournalPage() throws Exception {
    when(paymentQueryUseCase.search(any()))
        .thenReturn(
            new PaymentQueryResult(List.of(payment(9001L, PaymentStatus.COMPLETED)), 1, 0, 20));

    mockMvc
        .perform(get("/api/v1/payments").param("status", "COMPLETED"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content[0].id").value(9001))
        .andExpect(jsonPath("$.data.page.totalElements").value(1));
  }

  @Test
  void shouldVoidPayment() throws Exception {
    when(voidPaymentUseCase.voidPayment(any())).thenReturn(payment(9001L, PaymentStatus.VOIDED));

    mockMvc
        .perform(
            post("/api/v1/payments/9001/void")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"Cobro duplicado\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("VOIDED"))
        .andExpect(jsonPath("$.data.voidReason").value("This payment was voided"));
  }

  private void authenticate(List<String> areaTypes) {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new JwtAuthenticationToken(
                Jwt.withTokenValue("test-token")
                    .header("alg", "none")
                    .subject("cashier@test.com")
                    .claim("type", "access")
                    .claim("role", "WORKER")
                    .claim("areaTypes", areaTypes)
                    .build()));
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

  private static Payment payment(Long id, PaymentStatus status) {
    return Payment.builder()
        .id(id)
        .orderId(201767L)
        .shiftId(12L)
        .status(status)
        .receiptNumber("R-20261121-009001")
        .subtotal(money("58000.00"))
        .tip(money("5000.00"))
        .total(money("63000.00"))
        .tendered(money("70000.00"))
        .change(money("7000.00"))
        .tenders(
            List.of(
                new PaymentTender(
                    null, PaymentTenderMethod.CARD, money("50000.00"), "AUTH-4421", 0),
                new PaymentTender(null, PaymentTenderMethod.CASH, money("20000.00"), null, 1)))
        .cashierId(1001L)
        .createdAt(LocalDateTime.of(2026, 11, 21, 20, 14))
        .completedAt(LocalDateTime.of(2026, 11, 21, 20, 14))
        .voidedAt(status == PaymentStatus.VOIDED ? LocalDateTime.now() : null)
        .voidedBy(status == PaymentStatus.VOIDED ? 1002L : null)
        .voidReason(status == PaymentStatus.VOIDED ? "This payment was voided" : null)
        .build();
  }

  private static Money money(String amount) {
    return new Money(new BigDecimal(amount), COP);
  }

  private static final String CHARGE_BODY =
      """
      {
        "orderId": 201767,
        "tipAmount": "5000.00",
        "tenders": [
          { "method": "CARD", "amount": "50000.00", "reference": "AUTH-4421" },
          { "method": "CASH", "amount": "20000.00" }
        ]
      }
      """;
}
