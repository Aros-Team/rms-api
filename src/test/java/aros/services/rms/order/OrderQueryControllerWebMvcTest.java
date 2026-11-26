/* (C) 2026 */

package aros.services.rms.order;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.order.application.exception.OrderDetailNotFoundException;
import aros.services.rms.core.order.application.exception.OrderNotFoundException;
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

/**
 * Tests for {@link OrderQueryController}.
 *
 * <p><b>Feature:</b> Order REST endpoint (take, query, update)
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>should Return200 when Querying Orders With Defaults
 *   <li>should Return200 when Filtering By Single Status
 *   <li>should Return200 when Filtering By Multiple Statuses
 *   <li>should Return200 when Custom Page And Size
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
        new OrderResponse(
            1L, LocalDateTime.now(), "QUEUE", null, null, null, null, null, null, null, List.of());
    when(orderResponseMapper.toResponse(any())).thenReturn(dummyResponse);
    when(orderResponseMapper.toResponses(anyList()))
        .thenAnswer(
            invocation -> {
              List<?> orders = invocation.getArgument(0);
              return orders.stream().map(order -> dummyResponse).toList();
            });
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

    when(orderQueryUseCase.findOrdersPage(
            anyList(), any(), any(), any(), anyInt(), anyInt(), anyString()))
        .thenReturn(result);

    mockMvc
        .perform(get(URL).with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content").isArray())
        .andExpect(jsonPath("$.data.page.totalElements").value(1))
        .andExpect(jsonPath("$.data.page.number").value(0))
        .andExpect(jsonPath("$.data.page.size").value(20))
        .andExpect(jsonPath("$.data.page.totalPages").value(1));
  }

  // ---------------------------------------------------------------------------
  // 200: with status filter
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_whenFilteringBySingleStatus() throws Exception {
    Order order =
        Order.builder().id(2L).status(OrderStatus.QUEUE).date(LocalDateTime.now()).build();
    OrderQueryResult result = new OrderQueryResult(List.of(order), 1L, 0, 20);

    when(orderQueryUseCase.findOrdersPage(
            anyList(), any(), any(), any(), anyInt(), anyInt(), anyString()))
        .thenReturn(result);

    mockMvc
        .perform(get(URL).param("status", "QUEUE").with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content[0].status").value("QUEUE"))
        .andExpect(jsonPath("$.data.page.totalElements").value(1));
  }

  // ---------------------------------------------------------------------------
  // 200: with comma-separated statuses
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_whenFilteringByMultipleStatuses() throws Exception {
    OrderQueryResult result = new OrderQueryResult(List.of(), 0L, 0, 20);

    when(orderQueryUseCase.findOrdersPage(
            anyList(), any(), any(), any(), anyInt(), anyInt(), anyString()))
        .thenReturn(result);

    mockMvc
        .perform(get(URL).param("statuses", "QUEUE,READY").with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.page.totalElements").value(0));
  }

  // ---------------------------------------------------------------------------
  // 200: custom pagination
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_whenCustomPageAndSize() throws Exception {
    Order order =
        Order.builder().id(3L).status(OrderStatus.DELIVERED).date(LocalDateTime.now()).build();
    OrderQueryResult result = new OrderQueryResult(List.of(order), 50L, 2, 10);

    when(orderQueryUseCase.findOrdersPage(
            anyList(), any(), any(), any(), anyInt(), anyInt(), anyString()))
        .thenReturn(result);

    mockMvc
        .perform(get(URL).param("page", "2").param("size", "10").with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.page.number").value(2))
        .andExpect(jsonPath("$.data.page.size").value(10))
        .andExpect(jsonPath("$.data.page.totalElements").value(50))
        .andExpect(jsonPath("$.data.page.totalPages").value(5));
  }

  @Test
  void shouldReturn200_whenFilteringByProductOrOptionName() throws Exception {
    Order order =
        Order.builder().id(4L).status(OrderStatus.QUEUE).date(LocalDateTime.now()).build();
    OrderQueryResult result = new OrderQueryResult(List.of(order), 1L, 0, 20);

    when(orderQueryUseCase.findOrdersPage(
            anyList(), anyString(), any(), any(), any(), anyInt(), anyInt(), anyString()))
        .thenReturn(result);

    mockMvc
        .perform(get(URL).param("search", "burger").with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.page.totalElements").value(1))
        .andExpect(jsonPath("$.data.content[0].status").value("QUEUE"));
  }

  @Test
  void shouldReturn200_whenSearchHasNoResults() throws Exception {
    when(orderQueryUseCase.findOrdersPage(
            anyList(), anyString(), any(), any(), any(), anyInt(), anyInt(), anyString()))
        .thenReturn(new OrderQueryResult(List.of(), 0L, 0, 20));

    mockMvc
        .perform(get(URL).param("search", "does-not-exist").with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content").isEmpty())
        .andExpect(jsonPath("$.data.page.totalElements").value(0));
  }

  @Test
  void shouldReturn200_whenCombiningSearchWithStatusAndDateRange() throws Exception {
    when(orderQueryUseCase.findOrdersPage(
            anyList(), anyString(), any(), any(), any(), anyInt(), anyInt(), anyString()))
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
        .andExpect(jsonPath("$.data.page.totalElements").value(0));
  }

  @Test
  void shouldPreserveCurrentBehavior_whenSearchIsBlank() throws Exception {
    OrderQueryResult result = new OrderQueryResult(List.of(), 0L, 0, 20);
    when(orderQueryUseCase.findOrdersPage(
            anyList(), any(), any(), any(), anyInt(), anyInt(), anyString()))
        .thenReturn(result);

    mockMvc
        .perform(get(URL).param("search", "   ").with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content").isEmpty())
        .andExpect(jsonPath("$.data.page.totalElements").value(0));
  }

  // ---------------------------------------------------------------------------
  // 200: station (preparation area) filter
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_whenFilteringByStationArea() throws Exception {
    Order order =
        Order.builder().id(5L).status(OrderStatus.QUEUE).date(LocalDateTime.now()).build();
    OrderQueryResult result = new OrderQueryResult(List.of(order), 1L, 0, 20);

    when(orderQueryUseCase.findOrdersPage(
            anyList(), any(), any(), eq(1L), anyInt(), anyInt(), anyString()))
        .thenReturn(result);

    mockMvc
        .perform(get(URL).param("areaId", "1").with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.page.totalElements").value(1));

    verify(orderQueryUseCase)
        .findOrdersPage(anyList(), any(), any(), eq(1L), anyInt(), anyInt(), anyString());
  }

  @Test
  void shouldCombineStationAreaWithSearch() throws Exception {
    when(orderQueryUseCase.findOrdersPage(
            anyList(), anyString(), any(), any(), eq(3L), anyInt(), anyInt(), anyString()))
        .thenReturn(new OrderQueryResult(List.of(), 0L, 0, 20));

    mockMvc
        .perform(get(URL).param("areaId", "3").param("search", "burger").with(adminJwt()))
        .andExpect(status().isOk());

    verify(orderQueryUseCase)
        .findOrdersPage(
            anyList(), anyString(), any(), any(), eq(3L), anyInt(), anyInt(), anyString());
  }

  // ---------------------------------------------------------------------------
  // prepare a specific order
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_whenPreparingSpecificOrder() throws Exception {
    Order order =
        Order.builder().id(7L).status(OrderStatus.PREPARING).date(LocalDateTime.now()).build();
    when(preparationUseCase.prepareOrder(7L)).thenReturn(order);

    mockMvc
        .perform(put(URL + "/7/prepare").with(kitchenJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value(1));

    verify(preparationUseCase).prepareOrder(7L);
    verify(orderNotificationService).notifyOrderPreparing(any());
  }

  @Test
  void shouldReturn404_whenPreparingUnknownOrder() throws Exception {
    when(preparationUseCase.prepareOrder(999L)).thenThrow(new OrderNotFoundException(999L));

    mockMvc
        .perform(put(URL + "/999/prepare").with(kitchenJwt()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("Pedido no encontrado: 999"));
  }

  @Test
  void shouldReturn409_whenOrderIsNotInQueue() throws Exception {
    when(preparationUseCase.prepareOrder(8L))
        .thenThrow(
            new IllegalStateException(
                "El pedido solo se puede preparar cuando está en estado QUEUE"));

    mockMvc
        .perform(put(URL + "/8/prepare").with(kitchenJwt()))
        .andExpect(status().isConflict())
        .andExpect(
            jsonPath("$.detail")
                .value("El pedido solo se puede preparar cuando está en estado QUEUE"));
  }

  @Test
  void shouldReturn403_whenWaiterPreparesSpecificOrder() throws Exception {
    mockMvc.perform(put(URL + "/7/prepare").with(waiterJwt())).andExpect(status().isForbidden());
  }

  // ---------------------------------------------------------------------------
  // item-level readiness
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_whenMarkingDetailReady() throws Exception {
    Order order =
        Order.builder().id(7L).status(OrderStatus.PREPARING).date(LocalDateTime.now()).build();
    when(markAsReadyUseCase.markDetailAsReady(7L, 10L)).thenReturn(order);

    mockMvc
        .perform(put(URL + "/7/details/10/ready").with(kitchenJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value(1));

    verify(markAsReadyUseCase).markDetailAsReady(7L, 10L);
    verify(orderNotificationService, never()).notifyOrderReady(any());
  }

  @Test
  void shouldNotifyOrderReady_whenLastDetailCompletesOrder() throws Exception {
    Order order =
        Order.builder().id(7L).status(OrderStatus.READY).date(LocalDateTime.now()).build();
    when(markAsReadyUseCase.markDetailAsReady(7L, 10L)).thenReturn(order);

    mockMvc.perform(put(URL + "/7/details/10/ready").with(kitchenJwt())).andExpect(status().isOk());

    verify(orderNotificationService).notifyOrderReady(any());
  }

  @Test
  void shouldReturn404_whenMarkingUnknownDetailReady() throws Exception {
    when(markAsReadyUseCase.markDetailAsReady(7L, 404L))
        .thenThrow(new OrderDetailNotFoundException(7L, 404L));

    mockMvc
        .perform(put(URL + "/7/details/404/ready").with(kitchenJwt()))
        .andExpect(status().isNotFound())
        .andExpect(
            jsonPath("$.detail").value("Detalle del pedido no encontrado: 404 en el pedido 7"));
  }

  @Test
  void shouldReturn409_whenOrderDoesNotAllowDetailReadiness() throws Exception {
    when(markAsReadyUseCase.markDetailAsReady(9L, 10L))
        .thenThrow(
            new IllegalStateException(
                "Los detalles del pedido solo se pueden marcar como listos cuando están "
                    + "en estado PREPARING"));

    mockMvc
        .perform(put(URL + "/9/details/10/ready").with(kitchenJwt()))
        .andExpect(status().isConflict());
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

  private static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor kitchenJwt() {
    return jwt()
        .jwt(
            builder ->
                builder
                    .subject("cook@test.com")
                    .claim("role", "WORKER")
                    .claim("areaTypes", List.of("KITCHEN")))
        .authorities(new SimpleGrantedAuthority("ROLE_USER"));
  }

  private static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor waiterJwt() {
    return jwt()
        .jwt(
            builder ->
                builder
                    .subject("waiter@test.com")
                    .claim("role", "WORKER")
                    .claim("areaTypes", List.of("SERVICE")))
        .authorities(new SimpleGrantedAuthority("ROLE_USER"));
  }
}
