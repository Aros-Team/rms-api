/* (C) 2026 */

package aros.services.rms.order;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.image.port.output.StoragePort;
import aros.services.rms.core.inventory.application.exception.InsufficientStockException;
import aros.services.rms.core.inventory.domain.MovementType;
import aros.services.rms.core.order.application.dto.TakeOrderCommand;
import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.core.order.port.input.TakeOrderUseCase;
import aros.services.rms.core.table.domain.Table;
import aros.services.rms.core.table.domain.TableStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Endpoint tests for POST /api/v1/orders.
 *
 * <p>E-P-01: shouldReturn201_whenOrderIsCreatedSuccessfully E-P-02:
 * shouldReturn400_whenTableNotFound E-P-03: shouldReturn409_whenTableIsOccupied E-P-04:
 * shouldReturn400_whenProductNotFound E-P-05: shouldReturn409_whenInsufficientStock
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@TestPropertySource(
    properties = {
      "spring.flyway.enabled=false",
      "spring.jpa.hibernate.ddl-auto=create-drop",
      "spring.sql.init.mode=never",
      "app.admin.email=admin@test.local",
      "app.admin.dummy-email=test@test.local",
      "app.admin.password=TestPassword123!"
    })
class OrderControllerTest {

  @Autowired private WebApplicationContext context;

  @MockitoBean private TakeOrderUseCase takeOrderUseCase;
  @MockitoBean private StoragePort storagePort;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
  }

  private static final String VALID_BODY =
      """
      {
        "tableId": 1,
        "details": [
          {
            "productId": 1,
            "instructions": "Sin cebolla",
            "selectedOptionIds": []
          }
        ]
      }
      """;

  // ---------------------------------------------------------------------------
  // E-P-01: shouldReturn201_whenOrderIsCreatedSuccessfully
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn201_whenOrderIsCreatedSuccessfully() throws Exception {
    Table table = Table.builder().id(1L).status(TableStatus.OCCUPIED).build();
    Order order =
        Order.builder()
            .id(42L)
            .date(LocalDateTime.now())
            .status(OrderStatus.QUEUE)
            .table(table)
            .details(List.of())
            .build();

    when(takeOrderUseCase.execute(any(TakeOrderCommand.class))).thenReturn(order);

    mockMvc
        .perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(42))
        .andExpect(jsonPath("$.status").value("QUEUE"))
        .andExpect(jsonPath("$.tableId").value(1));
  }

  // ---------------------------------------------------------------------------
  // E-P-02: shouldReturn400_whenTableNotFound
  // TakeOrderService lanza IllegalArgumentException → GlobalExceptionHandler → 400
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn400_whenTableNotFound() throws Exception {
    when(takeOrderUseCase.execute(any(TakeOrderCommand.class)))
        .thenThrow(new IllegalArgumentException("Table not found"));

    mockMvc
        .perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Table not found"));
  }

  // ---------------------------------------------------------------------------
  // E-P-03: shouldReturn409_whenTableIsOccupied
  // TakeOrderService lanza IllegalStateException → GlobalExceptionHandler → 409
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn409_whenTableIsOccupied() throws Exception {
    when(takeOrderUseCase.execute(any(TakeOrderCommand.class)))
        .thenThrow(new IllegalStateException("Table is not available"));

    mockMvc
        .perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("Table is not available"));
  }

  // ---------------------------------------------------------------------------
  // E-P-04: shouldReturn400_whenProductNotFound
  // TakeOrderService lanza IllegalArgumentException → GlobalExceptionHandler → 400
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn400_whenProductNotFound() throws Exception {
    when(takeOrderUseCase.execute(any(TakeOrderCommand.class)))
        .thenThrow(new IllegalArgumentException("Product not found"));

    String bodyWithInvalidProduct =
        """
        {
          "tableId": 1,
          "details": [
            {
              "productId": 9999,
              "instructions": null,
              "selectedOptionIds": []
            }
          ]
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bodyWithInvalidProduct))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Product not found"));
  }

  // ---------------------------------------------------------------------------
  // E-P-05: shouldReturn409_whenInsufficientStock
  // TakeOrderService lanza InsufficientStockException → GlobalExceptionHandler → 409
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn409_whenInsufficientStock() throws Exception {
    when(takeOrderUseCase.execute(any(TakeOrderCommand.class)))
        .thenThrow(
            new InsufficientStockException(
                1L, MovementType.DEDUCTION, "Insufficient stock for product: Test Product"));

    mockMvc
        .perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("Insufficient stock for product: Test Product"));
  }
}
