/* (C) 2026 */

package aros.services.rms.infraestructure.cashshift.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.area.domain.AreaActivity;
import aros.services.rms.core.auth.port.output.CurrentUserPort;
import aros.services.rms.core.cashshift.application.exception.ShiftClosedException;
import aros.services.rms.core.cashshift.domain.CashShift;
import aros.services.rms.core.cashshift.domain.CashShiftStatus;
import aros.services.rms.core.cashshift.domain.CashShiftSummary;
import aros.services.rms.core.cashshift.domain.CashShiftTotals;
import aros.services.rms.core.cashshift.port.input.CashShiftQueryUseCase;
import aros.services.rms.core.cashshift.port.input.CloseCashShiftUseCase;
import aros.services.rms.core.cashshift.port.input.OpenCashShiftUseCase;
import aros.services.rms.core.common.money.domain.Money;
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
 * Tests for {@link CashShiftController}: open, close and read the cash drawer.
 *
 * <p><b>Feature:</b> Open, close and read the cash drawer.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>Opening returns 201; the current shift returns 200 or 204
 *   <li>Closing returns the snapshotted expected/counted/difference summary
 *   <li>Closing an already closed shift returns 409 problem+json
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Loads the full context on H2 and mocks the cash-shift use cases
 *   <li>Drives MockMvc with a CASH worker principal
 * </ul>
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = {
      "spring.flyway.enabled=false",
      "spring.jpa.hibernate.ddl-auto=create-drop",
      "spring.sql.init.mode=never",
      "spring.datasource.url=jdbc:h2:mem:cashshiftdb;DB_CLOSE_DELAY=-1;MODE=MYSQL",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.datasource.username=sa",
      "spring.datasource.password=sa",
      "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
      "app.admin.email=admin@test.local",
      "app.admin.dummy-email=test@test.local",
      "app.admin.password=TestPassword123!",
      "app.env=development"
    })
class CashShiftControllerTest extends AbstractJwtIntegrationTest {

  private static final Currency COP = Currency.getInstance("COP");

  @Autowired private WebApplicationContext context;

  @MockitoBean private OpenCashShiftUseCase openCashShiftUseCase;
  @MockitoBean private CloseCashShiftUseCase closeCashShiftUseCase;
  @MockitoBean private CashShiftQueryUseCase cashShiftQueryUseCase;
  @MockitoBean private CurrentUserPort currentUserPort;
  @MockitoBean private CurrencyProvider currencyProvider;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    SecurityContextHolder.getContext()
        .setAuthentication(
            new JwtAuthenticationToken(
                Jwt.withTokenValue("test-token")
                    .header("alg", "none")
                    .subject("cashier@test.com")
                    .claim("type", "access")
                    .claim("role", "WORKER")
                    .claim("areaTypes", List.of(AreaActivity.CASH.name()))
                    .build()));
    when(currentUserPort.fetchCurrentUserInfo()).thenReturn(Optional.of(cashier()));
    when(currencyProvider.getCurrency()).thenReturn(COP);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldOpenShift() throws Exception {
    when(openCashShiftUseCase.open(any())).thenReturn(shift(CashShiftStatus.OPEN));

    mockMvc
        .perform(
            post("/api/v1/cash-shifts/open")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"openingFloat\":\"200000.00\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id").value(12))
        .andExpect(jsonPath("$.data.status").value("OPEN"))
        .andExpect(jsonPath("$.data.openingFloat.amount").value("200000.00"));
  }

  @Test
  void shouldReturnTheCurrentShift() throws Exception {
    when(cashShiftQueryUseCase.findCurrent()).thenReturn(Optional.of(shift(CashShiftStatus.OPEN)));

    mockMvc
        .perform(get("/api/v1/cash-shifts/current"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("OPEN"));
  }

  @Test
  void shouldReturn204WhenNoShiftIsOpen() throws Exception {
    when(cashShiftQueryUseCase.findCurrent()).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/v1/cash-shifts/current")).andExpect(status().isNoContent());
  }

  @Test
  void shouldCloseShiftAndReturnReconciliation() throws Exception {
    when(closeCashShiftUseCase.close(any())).thenReturn(closedSummary());

    mockMvc
        .perform(
            post("/api/v1/cash-shifts/12/close")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"countedCash\":\"95000.00\",\"notes\":\"Cierre turno noche\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("CLOSED"))
        .andExpect(jsonPath("$.data.expectedCash.amount").value("100000.00"))
        .andExpect(jsonPath("$.data.countedCash.amount").value("95000.00"))
        .andExpect(jsonPath("$.data.difference.amount").value("-5000.00"));
  }

  @Test
  void shouldReturn409WhenTheShiftIsAlreadyClosed() throws Exception {
    when(closeCashShiftUseCase.close(any())).thenThrow(new ShiftClosedException(12L));

    mockMvc
        .perform(
            post("/api/v1/cash-shifts/12/close")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"countedCash\":\"95000.00\"}"))
        .andExpect(status().isConflict())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
  }

  @Test
  void shouldReturnTheShiftSummary() throws Exception {
    when(cashShiftQueryUseCase.findSummary(12L)).thenReturn(closedSummary());

    mockMvc
        .perform(get("/api/v1/cash-shifts/12"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.paymentsCount").value(0))
        .andExpect(jsonPath("$.data.difference.amount").value("-5000.00"));
  }

  private static CashShiftSummary closedSummary() {
    CashShift shift = shift(CashShiftStatus.CLOSED);
    Money expected = money("100000.00");
    Money counted = money("95000.00");
    shift.setExpectedCash(expected);
    shift.setCountedCash(counted);
    shift.setDifference(counted.minus(expected));
    shift.setClosedAt(LocalDateTime.of(2026, 11, 21, 22, 41));
    shift.setClosedBy(1001L);
    CashShiftTotals totals = CashShiftTotals.from(List.of(), List.of(), money("100000.00"));
    return new CashShiftSummary(shift, totals);
  }

  private static CashShift shift(CashShiftStatus status) {
    return CashShift.builder()
        .id(12L)
        .status(status)
        .openingFloat(money("200000.00"))
        .openedBy(1001L)
        .openedAt(LocalDateTime.of(2026, 11, 21, 11, 2))
        .build();
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

  private static Money money(String amount) {
    return new Money(new BigDecimal(amount), COP);
  }
}
