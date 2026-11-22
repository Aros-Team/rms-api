/* (C) 2026 */

package aros.services.rms.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.auth.port.input.AccountSetupUseCase;
import aros.services.rms.core.category.port.input.OptionGroupUseCase;
import aros.services.rms.core.image.port.output.ImageRepositoryPort;
import aros.services.rms.core.image.port.output.StoragePort;
import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderQueryResult;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.core.order.port.input.DeliveryUseCase;
import aros.services.rms.core.order.port.input.MarkAsReadyUseCase;
import aros.services.rms.core.order.port.input.OrderQueryUseCase;
import aros.services.rms.core.order.port.input.PreparationUseCase;
import aros.services.rms.core.order.port.input.TakeOrderUseCase;
import aros.services.rms.core.order.port.input.UpdateOrderUseCase;
import aros.services.rms.core.payroll.domain.Payroll;
import aros.services.rms.core.payroll.domain.port.input.DeletePayrollUseCase;
import aros.services.rms.core.payroll.domain.port.input.GetPayrollUseCase;
import aros.services.rms.core.payroll.domain.port.input.ListPayrollsUseCase;
import aros.services.rms.core.payroll.domain.port.input.RegisterPayrollUseCase;
import aros.services.rms.core.payroll.domain.port.input.UpdatePayrollUseCase;
import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.port.input.CalculateProductCostUseCase;
import aros.services.rms.core.product.port.input.GetProductCostBreakdownUseCase;
import aros.services.rms.core.product.port.input.ProductUseCase;
import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;
import aros.services.rms.core.specialselection.port.input.CreateSpecialSelectionUseCase;
import aros.services.rms.core.specialselection.port.input.DeleteSpecialSelectionUseCase;
import aros.services.rms.core.specialselection.port.input.GetSpecialSelectionHistoryUseCase;
import aros.services.rms.core.specialselection.port.input.GetSpecialSelectionUseCase;
import aros.services.rms.core.specialselection.port.input.RevertSpecialSelectionUseCase;
import aros.services.rms.core.specialselection.port.input.SuggestSpecialSelectionPriceUseCase;
import aros.services.rms.core.specialselection.port.input.UpdateSpecialSelectionActiveUseCase;
import aros.services.rms.core.specialselection.port.input.UpdateSpecialSelectionPriceUseCase;
import aros.services.rms.core.specialselection.port.input.UpdateSpecialSelectionScheduleUseCase;
import aros.services.rms.core.specialselection.port.input.UpdateSpecialSelectionUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.GetSystemConfigurationUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.ManageConfigurationUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateSystemConfigurationUseCase;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import aros.services.rms.core.table.domain.Table;
import aros.services.rms.core.table.domain.TableStatus;
import aros.services.rms.core.table.port.input.TableUseCase;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserEmail;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.domain.UserRole;
import aros.services.rms.core.user.domain.UserStatus;
import aros.services.rms.core.user.port.input.CreateUserUseCase;
import aros.services.rms.core.user.port.input.DeleteUserUseCase;
import aros.services.rms.core.user.port.input.GetAllWorkersUseCase;
import aros.services.rms.core.user.port.input.GetSalaryHistoryUseCase;
import aros.services.rms.core.user.port.input.UpdateUserUseCase;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import aros.services.rms.infraestructure.common.config.RestApiConfig;
import aros.services.rms.infraestructure.common.exception.GlobalExceptionHandler;
import aros.services.rms.infraestructure.image.storage.local.LocalResourceConfig;
import aros.services.rms.infraestructure.order.api.OrderController;
import aros.services.rms.infraestructure.order.api.OrderNotificationService;
import aros.services.rms.infraestructure.order.api.dto.OrderResponse;
import aros.services.rms.infraestructure.order.api.dto.OrderResponseMapper;
import aros.services.rms.infraestructure.payroll.api.PayrollController;
import aros.services.rms.infraestructure.product.api.ProductController;
import aros.services.rms.infraestructure.specialselection.api.SpecialSelectionAdminController;
import aros.services.rms.infraestructure.specialselection.api.SpecialSelectionController;
import aros.services.rms.infraestructure.specialselection.api.SpecialSelectionNotificationService;
import aros.services.rms.infraestructure.systemconfig.api.SystemConfigurationController;
import aros.services.rms.infraestructure.table.api.TableController;
import aros.services.rms.infraestructure.table.api.TableNotificationService;
import aros.services.rms.infraestructure.user.api.WorkerController;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Table-driven authorization matrix across the API surface.
 *
 * <p>Tests for {@link AuthorizationMatrixTest}: role and area enforcement per endpoint.
 *
 * <p><b>Feature:</b> Role and area authorization on representative endpoints.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>Every protected endpoint allows the intended role/area.
 *   <li>Every protected endpoint rejects an authenticated user from another role/area with 403.
 *   <li>Forbidden responses use the RFC 7807 {@code application/problem+json} contract.
 *   <li>Public endpoints stay public; everything else requires a token.
 *   <li>The documented F-006 transition keeps only the combo detail read open to workers.
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Loads the affected controllers in a WebMvc slice with method security enabled.
 *   <li>Drives one request per matrix row, attaching a JWT with the role and areaTypes claims.
 *   <li>Asserts the HTTP status and, for denials, the RFC 7807 body.
 * </ul>
 */
@WebMvcTest(
    controllers = {
      PayrollController.class,
      SystemConfigurationController.class,
      ProductController.class,
      TableController.class,
      OrderController.class,
      WorkerController.class,
      SpecialSelectionAdminController.class,
      SpecialSelectionController.class
    },
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = LocalResourceConfig.class))
@Import({
  AuthorizationMatrixTest.TestSecurityConfig.class,
  GlobalExceptionHandler.class,
  RestApiConfig.class
})
class AuthorizationMatrixTest {

  private static final Currency COP = Currency.getInstance("COP");

  @Autowired private MockMvc mockMvc;

  @MockitoBean private RegisterPayrollUseCase registerPayrollUseCase;
  @MockitoBean private GetPayrollUseCase getPayrollUseCase;
  @MockitoBean private ListPayrollsUseCase listPayrollsUseCase;
  @MockitoBean private UpdatePayrollUseCase updatePayrollUseCase;
  @MockitoBean private DeletePayrollUseCase deletePayrollUseCase;
  @MockitoBean private CurrencyProvider currencyProvider;

  @MockitoBean private GetSystemConfigurationUseCase getSystemConfigurationUseCase;
  @MockitoBean private UpdateSystemConfigurationUseCase updateSystemConfigurationUseCase;
  @MockitoBean private ManageConfigurationUseCase manageConfigurationUseCase;

  @MockitoBean private ProductUseCase productUseCase;
  @MockitoBean private CalculateProductCostUseCase calculateProductCostUseCase;
  @MockitoBean private GetProductCostBreakdownUseCase getProductCostBreakdownUseCase;
  @MockitoBean private ImageRepositoryPort imageRepositoryPort;
  @MockitoBean private StoragePort storagePort;
  @MockitoBean private OptionGroupUseCase optionGroupUseCase;

  @MockitoBean
  private aros.services.rms.core.analytics.domain.port.in.GetTopSellingProductsUseCase
      getTopSellingProductsUseCase;

  @MockitoBean private TableUseCase tableUseCase;
  @MockitoBean private TableNotificationService tableNotificationService;

  @MockitoBean private TakeOrderUseCase takeOrderUseCase;
  @MockitoBean private UpdateOrderUseCase updateOrderUseCase;
  @MockitoBean private PreparationUseCase preparationUseCase;
  @MockitoBean private MarkAsReadyUseCase markAsReadyUseCase;
  @MockitoBean private DeliveryUseCase deliveryUseCase;
  @MockitoBean private OrderQueryUseCase orderQueryUseCase;
  @MockitoBean private OrderNotificationService orderNotificationService;
  @MockitoBean private OrderResponseMapper orderResponseMapper;

  @MockitoBean private CreateUserUseCase createUserUseCase;
  @MockitoBean private GetAllWorkersUseCase getAllWorkersUseCase;
  @MockitoBean private UpdateUserUseCase updateUserUseCase;
  @MockitoBean private DeleteUserUseCase deleteUserUseCase;
  @MockitoBean private AccountSetupUseCase accountSetupUseCase;
  @MockitoBean private UserRepositoryPort userRepositoryPort;
  @MockitoBean private GetSalaryHistoryUseCase getSalaryHistoryUseCase;

  @MockitoBean private CreateSpecialSelectionUseCase createSpecialSelectionUseCase;
  @MockitoBean private UpdateSpecialSelectionUseCase updateSpecialSelectionUseCase;
  @MockitoBean private UpdateSpecialSelectionActiveUseCase updateSpecialSelectionActiveUseCase;
  @MockitoBean private UpdateSpecialSelectionPriceUseCase updateSpecialSelectionPriceUseCase;
  @MockitoBean private UpdateSpecialSelectionScheduleUseCase updateSpecialSelectionScheduleUseCase;
  @MockitoBean private DeleteSpecialSelectionUseCase deleteSpecialSelectionUseCase;
  @MockitoBean private GetSpecialSelectionUseCase getSpecialSelectionUseCase;
  @MockitoBean private GetSpecialSelectionHistoryUseCase getSpecialSelectionHistoryUseCase;
  @MockitoBean private RevertSpecialSelectionUseCase revertSpecialSelectionUseCase;
  @MockitoBean private SuggestSpecialSelectionPriceUseCase suggestSpecialSelectionPriceUseCase;
  @MockitoBean private SpecialSelectionNotificationService specialSelectionNotificationService;

  @MockitoBean private JwtDecoder jwtDecoder;

  private static final String ORDERS_URL = "/api/v1/orders";

  @BeforeEach
  void setUp() {
    when(currencyProvider.getCurrency()).thenReturn(COP);

    User self =
        new User(
            UserId.of(42L),
            "1000",
            "Self Worker",
            new UserEmail("self@test.com"),
            "encoded",
            "Address",
            "555-0100",
            UserRole.WORKER,
            UserStatus.ACTIVE,
            List.of());
    when(userRepositoryPort.findByEmail("self@test.com")).thenReturn(Optional.of(self));

    when(getSalaryHistoryUseCase.getSalaryHistory(anyLong())).thenReturn(List.of());
    when(listPayrollsUseCase.findAll()).thenReturn(List.<Payroll>of());
    when(listPayrollsUseCase.findByUserId(anyLong())).thenReturn(List.<Payroll>of());
    when(getSystemConfigurationUseCase.findAllGroups()).thenReturn(List.of());
    when(updateSystemConfigurationUseCase.saveAll(any())).thenReturn(List.of());

    Product product =
        Product.builder()
            .id(9L)
            .name("Product")
            .basePrice(
                new aros.services.rms.core.common.money.domain.Money(new BigDecimal("10.00"), COP))
            .active(true)
            .build();
    when(productUseCase.create(any())).thenReturn(product);
    Pageable pageable = PageRequest.of(0, 20);
    when(productUseCase.findAllActive(any(Pageable.class), anyBoolean()))
        .thenReturn(new PageImpl<>(List.of(product), pageable, 1));

    Table table =
        Table.builder().id(1L).tableNumber(1).capacity(4).status(TableStatus.OCCUPIED).build();
    when(tableUseCase.create(any())).thenReturn(table);
    when(tableUseCase.findAll()).thenReturn(List.of(table));
    when(tableUseCase.changeStatus(anyLong(), any())).thenReturn(table);

    Order order =
        Order.builder()
            .id(1L)
            .status(OrderStatus.PREPARING)
            .date(LocalDateTime.now())
            .details(List.of())
            .build();
    when(takeOrderUseCase.execute(any())).thenReturn(order);
    when(markAsReadyUseCase.markAsReady(anyLong())).thenReturn(order);
    when(markAsReadyUseCase.markDetailAsReady(anyLong(), anyLong())).thenReturn(order);
    when(preparationUseCase.prepareOrder(anyLong())).thenReturn(order);
    when(deliveryUseCase.markAsDelivered(anyLong())).thenReturn(order);
    when(orderQueryUseCase.findOrdersPage(
            anyList(), any(), any(), any(), anyInt(), anyInt(), anyString()))
        .thenReturn(new OrderQueryResult(List.of(), 0L, 0, 20));
    OrderResponse orderResponse =
        new OrderResponse(
            1L, LocalDateTime.now(), "PREPARING", null, null, null, null, null, List.of());
    when(orderResponseMapper.toResponse(any())).thenReturn(orderResponse);

    SpecialSelectionConfiguration combo =
        SpecialSelectionConfiguration.builder()
            .productId(1L)
            .name("Combo")
            .basePrice(10.0)
            .active(true)
            .groups(List.of())
            .additions(List.of())
            .questions(List.of())
            .schedule(List.of())
            .build();
    when(getSpecialSelectionUseCase.findById(1L)).thenReturn(Optional.of(combo));
    when(getSpecialSelectionUseCase.findAvailableNow()).thenReturn(List.of(combo));
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(MatrixCases.class)
  void should_enforce_authorization_matrix(MatrixCase matrixCase) throws Exception {
    MockHttpServletRequestBuilder request = matrixCase.request();
    if (matrixCase.auth() != null) {
      request = request.with(matrixCase.auth());
    }

    var result = mockMvc.perform(request);

    if (matrixCase.expectedStatus() == 403) {
      result
          .andExpect(status().isForbidden())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
          .andExpect(jsonPath("$.status").value(403));
    } else {
      result.andExpect(status().is(matrixCase.expectedStatus()));
    }
  }

  /** One representative endpoint, its allowed principal and its denied principal. */
  record MatrixCase(
      String label,
      MockHttpServletRequestBuilder request,
      JwtRequestPostProcessor auth,
      int expectedStatus) {}

  /** Builds the authorization matrix rows. */
  static class MatrixCases implements ArgumentsProvider {

    private static final String CONFIG_BODY = "[{\"key\":\"default_currency\",\"value\":\"COP\"}]";

    private static final String PRODUCT_BODY =
        """
        {"name":"Product","basePrice":10.0,"categoryId":1,"areaId":1}
        """;

    private static final String TABLE_BODY =
        """
        {"tableNumber":1,"capacity":4}
        """;

    private static final String ORDER_BODY =
        """
        {"tableId":1,"details":[{"productId":1}]}
        """;

    private static final String TABLE_STATUS_BODY = "{\"status\":\"OCCUPIED\"}";

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          arg("payroll list / ADMIN allowed", get("/api/v1/payroll"), admin(), 200),
          arg("payroll list / WORKER denied", get("/api/v1/payroll"), worker("SERVICE"), 403),
          arg("payroll by worker / ADMIN allowed", get("/api/v1/payroll/worker/7"), admin(), 200),
          arg(
              "payroll by worker / own WORKER allowed",
              get("/api/v1/payroll/worker/42"),
              selfWorker(),
              200),
          arg(
              "payroll by worker / other WORKER denied",
              get("/api/v1/payroll/worker/7"),
              selfWorker(),
              403),
          arg("config read / ADMIN allowed", get("/api/v1/config"), admin(), 200),
          arg("config read / WORKER denied", get("/api/v1/config"), worker("SERVICE"), 403),
          arg(
              "config write / ADMIN allowed",
              put("/api/v1/config").contentType(MediaType.APPLICATION_JSON).content(CONFIG_BODY),
              admin(),
              200),
          arg(
              "config write / WORKER denied",
              put("/api/v1/config").contentType(MediaType.APPLICATION_JSON).content(CONFIG_BODY),
              worker("SERVICE"),
              403),
          arg(
              "product create / ADMIN allowed",
              post("/api/v1/products")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(PRODUCT_BODY),
              admin(),
              201),
          arg(
              "product create / WORKER denied",
              post("/api/v1/products")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(PRODUCT_BODY),
              worker("SERVICE"),
              403),
          arg("product read / WORKER allowed", get("/api/v1/products"), worker("KITCHEN"), 200),
          arg(
              "table create / ADMIN allowed",
              post("/api/v1/tables").contentType(MediaType.APPLICATION_JSON).content(TABLE_BODY),
              admin(),
              201),
          arg(
              "table create / WORKER denied",
              post("/api/v1/tables").contentType(MediaType.APPLICATION_JSON).content(TABLE_BODY),
              worker("SERVICE"),
              403),
          arg(
              "table status / waiter allowed",
              put("/api/v1/tables/1/status")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(TABLE_STATUS_BODY),
              worker("SERVICE"),
              200),
          arg(
              "table status / kitchen denied",
              put("/api/v1/tables/1/status")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(TABLE_STATUS_BODY),
              worker("KITCHEN"),
              403),
          arg(
              "order create / waiter allowed",
              post(ORDERS_URL).contentType(MediaType.APPLICATION_JSON).content(ORDER_BODY),
              worker("SERVICE"),
              201),
          arg(
              "order create / kitchen denied",
              post(ORDERS_URL).contentType(MediaType.APPLICATION_JSON).content(ORDER_BODY),
              worker("KITCHEN"),
              403),
          arg(
              "order ready / kitchen allowed",
              put(ORDERS_URL + "/1/ready"),
              worker("KITCHEN"),
              200),
          arg("order ready / waiter denied", put(ORDERS_URL + "/1/ready"), worker("SERVICE"), 403),
          arg(
              "order prepare by id / kitchen allowed",
              put(ORDERS_URL + "/1/prepare"),
              worker("KITCHEN"),
              200),
          arg(
              "order prepare by id / waiter denied",
              put(ORDERS_URL + "/1/prepare"),
              worker("SERVICE"),
              403),
          arg(
              "order detail ready / kitchen allowed",
              put(ORDERS_URL + "/1/details/10/ready"),
              worker("KITCHEN"),
              200),
          arg(
              "order detail ready / waiter denied",
              put(ORDERS_URL + "/1/details/10/ready"),
              worker("SERVICE"),
              403),
          arg(
              "order deliver / cashier allowed",
              put(ORDERS_URL + "/1/deliver"),
              worker("CASH"),
              200),
          arg("order read / worker allowed", get(ORDERS_URL), worker("KITCHEN"), 200),
          arg(
              "salary history / ADMIN allowed",
              get("/api/v1/workers/42/salary-history"),
              admin(),
              200),
          arg(
              "salary history / own WORKER allowed",
              get("/api/v1/workers/42/salary-history"),
              selfWorker(),
              200),
          arg(
              "salary history / other WORKER denied",
              get("/api/v1/workers/42/salary-history"),
              worker("SERVICE"),
              403),
          arg(
              "combo admin detail / WORKER allowed (F-006 transition)",
              get("/api/v1/admin/special-selections/1"),
              worker("SERVICE"),
              200),
          arg(
              "combo admin list / WORKER denied",
              get("/api/v1/admin/special-selections"),
              worker("SERVICE"),
              403),
          arg(
              "available special selections / public",
              get("/api/v1/special-selections/available"),
              null,
              200),
          arg(
              "special selection detail / anonymous denied",
              get("/api/v1/special-selections/1"),
              null,
              401),
          arg("payroll list / anonymous denied", get("/api/v1/payroll"), null, 401));
    }

    private static Arguments arg(
        String label,
        MockHttpServletRequestBuilder request,
        JwtRequestPostProcessor auth,
        int expectedStatus) {
      return Arguments.of(new MatrixCase(label, request, auth, expectedStatus));
    }
  }

  private static JwtRequestPostProcessor admin() {
    return jwt()
        .jwt(
            builder ->
                builder.subject("admin@test.com").claim("type", "access").claim("role", "ADMIN"));
  }

  private static JwtRequestPostProcessor selfWorker() {
    return jwt()
        .jwt(
            builder ->
                builder
                    .subject("self@test.com")
                    .claim("type", "access")
                    .claim("role", "WORKER")
                    .claim("areaTypes", List.of("SERVICE")));
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

  @org.springframework.boot.test.context.TestConfiguration
  @EnableMethodSecurity
  static class TestSecurityConfig {
    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
      http.csrf(csrf -> csrf.disable())
          .sessionManagement(
              session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .authorizeHttpRequests(
              auth ->
                  auth.requestMatchers("/api/v1/special-selections/available")
                      .permitAll()
                      .anyRequest()
                      .authenticated())
          .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
      return http.build();
    }
  }
}
