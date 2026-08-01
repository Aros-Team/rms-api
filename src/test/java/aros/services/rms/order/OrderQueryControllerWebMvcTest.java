/* (C) 2026 */

package aros.services.rms.order;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderQueryResult;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.core.order.port.input.DeliveryUseCase;
import aros.services.rms.core.order.port.input.MarkAsReadyUseCase;
import aros.services.rms.core.order.port.input.OrderQueryUseCase;
import aros.services.rms.core.order.port.input.PreparationUseCase;
import aros.services.rms.core.order.port.input.TakeOrderUseCase;
import aros.services.rms.core.order.port.input.UpdateOrderUseCase;
import aros.services.rms.infraestructure.common.exception.GlobalExceptionHandler;
import aros.services.rms.infraestructure.image.storage.local.LocalResourceConfig;
import aros.services.rms.infraestructure.order.api.OrderController;
import aros.services.rms.infraestructure.order.api.OrderNotificationService;
import aros.services.rms.infraestructure.order.api.dto.OrderResponse;
import aros.services.rms.infraestructure.order.api.dto.OrderResponseMapper;
import aros.services.rms.infraestructure.table.api.TableNotificationService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Web MVC slice tests for GET /api/v1/orders with pagination. */
@WebMvcTest(
    value = OrderController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = LocalResourceConfig.class))
@Import({OrderQueryControllerWebMvcTest.TestSecurityConfig.class, GlobalExceptionHandler.class})
class OrderQueryControllerWebMvcTest {

  private static final String URL = "/api/v1/orders";

  @Autowired private MockMvc mockMvc;

  @MockitoBean private TakeOrderUseCase takeOrderUseCase;
  @MockitoBean private UpdateOrderUseCase updateOrderUseCase;
  @MockitoBean private PreparationUseCase preparationUseCase;
  @MockitoBean private MarkAsReadyUseCase markAsReadyUseCase;
  @MockitoBean private DeliveryUseCase deliveryUseCase;
  @MockitoBean private OrderQueryUseCase orderQueryUseCase;
  @MockitoBean private OrderNotificationService orderNotificationService;
  @MockitoBean private TableNotificationService tableNotificationService;
  @MockitoBean private OrderResponseMapper orderResponseMapper;
  @MockitoBean private JwtDecoder jwtDecoder;

  @BeforeEach
  void setUp() {
    OrderResponse dummyResponse =
        new OrderResponse(1L, LocalDateTime.now(), "QUEUE", null, null, null, null, List.of());
    when(orderResponseMapper.toResponse(any())).thenReturn(dummyResponse);
  }

  @org.springframework.boot.test.context.TestConfiguration
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

  // ---------------------------------------------------------------------------
  // 200: returns paginated orders
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_whenQueryingOrdersWithDefaults() throws Exception {
    Order order =
        Order.builder().id(1L).status(OrderStatus.QUEUE).date(LocalDateTime.now()).build();
    OrderQueryResult result = new OrderQueryResult(List.of(order), 1L, 0, 20);

    when(orderQueryUseCase.findOrdersPage(anyList(), any(), any(), anyInt(), anyInt(), anyString()))
        .thenReturn(result);

    mockMvc
        .perform(get(URL).with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.total").value(1))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(20))
        .andExpect(jsonPath("$.total_pages").value(1));
  }

  // ---------------------------------------------------------------------------
  // 200: with status filter
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_whenFilteringBySingleStatus() throws Exception {
    Order order =
        Order.builder().id(2L).status(OrderStatus.QUEUE).date(LocalDateTime.now()).build();
    OrderQueryResult result = new OrderQueryResult(List.of(order), 1L, 0, 20);

    when(orderQueryUseCase.findOrdersPage(anyList(), any(), any(), anyInt(), anyInt(), anyString()))
        .thenReturn(result);

    mockMvc
        .perform(get(URL).param("status", "QUEUE").with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items[0].status").value("QUEUE"))
        .andExpect(jsonPath("$.total").value(1));
  }

  // ---------------------------------------------------------------------------
  // 200: with comma-separated statuses
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_whenFilteringByMultipleStatuses() throws Exception {
    OrderQueryResult result = new OrderQueryResult(List.of(), 0L, 0, 20);

    when(orderQueryUseCase.findOrdersPage(anyList(), any(), any(), anyInt(), anyInt(), anyString()))
        .thenReturn(result);

    mockMvc
        .perform(get(URL).param("statuses", "QUEUE,READY").with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total").value(0));
  }

  // ---------------------------------------------------------------------------
  // 200: custom pagination
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_whenCustomPageAndSize() throws Exception {
    Order order =
        Order.builder().id(3L).status(OrderStatus.DELIVERED).date(LocalDateTime.now()).build();
    OrderQueryResult result = new OrderQueryResult(List.of(order), 50L, 2, 10);

    when(orderQueryUseCase.findOrdersPage(anyList(), any(), any(), anyInt(), anyInt(), anyString()))
        .thenReturn(result);

    mockMvc
        .perform(get(URL).param("page", "2").param("size", "10").with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.page").value(2))
        .andExpect(jsonPath("$.size").value(10))
        .andExpect(jsonPath("$.total").value(50))
        .andExpect(jsonPath("$.total_pages").value(5));
  }

  @Test
  void shouldReturn200_whenFilteringByProductOrOptionName() throws Exception {
    Order order =
        Order.builder().id(4L).status(OrderStatus.QUEUE).date(LocalDateTime.now()).build();
    OrderQueryResult result = new OrderQueryResult(List.of(order), 1L, 0, 20);

    when(orderQueryUseCase.findOrdersPage(
            anyList(), anyString(), any(), any(), anyInt(), anyInt(), anyString()))
        .thenReturn(result);

    mockMvc
        .perform(get(URL).param("search", "burger").with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total").value(1))
        .andExpect(jsonPath("$.items[0].status").value("QUEUE"));
  }

  @Test
  void shouldReturn200_whenSearchHasNoResults() throws Exception {
    when(orderQueryUseCase.findOrdersPage(
            anyList(), anyString(), any(), any(), anyInt(), anyInt(), anyString()))
        .thenReturn(new OrderQueryResult(List.of(), 0L, 0, 20));

    mockMvc
        .perform(get(URL).param("search", "does-not-exist").with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isEmpty())
        .andExpect(jsonPath("$.total").value(0));
  }

  @Test
  void shouldReturn200_whenCombiningSearchWithStatusAndDateRange() throws Exception {
    when(orderQueryUseCase.findOrdersPage(
            anyList(), anyString(), any(), any(), anyInt(), anyInt(), anyString()))
        .thenReturn(new OrderQueryResult(List.of(), 0L, 0, 20));

    mockMvc
        .perform(
            get(URL)
                .param("search", "burger")
                .param("status", "READY")
                .param("startDate", "2026-01-01T00:00:00")
                .param("endDate", "2026-01-31T23:59:59")
                .with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total").value(0));
  }

  @Test
  void shouldPreserveCurrentBehavior_whenSearchIsBlank() throws Exception {
    OrderQueryResult result = new OrderQueryResult(List.of(), 0L, 0, 20);
    when(orderQueryUseCase.findOrdersPage(anyList(), any(), any(), anyInt(), anyInt(), anyString()))
        .thenReturn(result);

    mockMvc
        .perform(get(URL).param("search", "   ").with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isEmpty())
        .andExpect(jsonPath("$.total").value(0));
  }

  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn401_whenUnauthenticated() throws Exception {
    mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());
  }

  // ---------------------------------------------------------------------------
  // 400: invalid status value
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn400_whenInvalidStatus() throws Exception {
    mockMvc
        .perform(get(URL).param("status", "INVALID").with(adminJwt()))
        .andExpect(status().isBadRequest());
  }

  // ---------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------

  private static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor adminJwt() {
    return jwt()
        .jwt(builder -> builder.subject("admin@test.com").claim("role", "ADMIN"))
        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
  }
}
