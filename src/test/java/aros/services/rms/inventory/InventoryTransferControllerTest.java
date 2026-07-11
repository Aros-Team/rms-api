/* (C) 2026 */

package aros.services.rms.inventory;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.inventory.application.exception.InsufficientStockException;
import aros.services.rms.core.inventory.domain.InventoryMovement;
import aros.services.rms.core.inventory.domain.MovementType;
import aros.services.rms.core.inventory.port.input.TransferInventoryUseCase;
import java.math.BigDecimal;
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
 * Endpoint tests for POST /api/v1/inventory/transfer.
 *
 * <p>E-I-07: shouldReturn200_whenTransferIsSuccessful E-I-08:
 * shouldReturn400_whenInsufficientStockInBodega
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
      "app.admin.password=TestPassword123!",
      "CORS_ALLOWED_ORIGINS=*",
    })
class InventoryTransferControllerTest {

  @Autowired private WebApplicationContext context;

  @MockitoBean private TransferInventoryUseCase transferInventoryUseCase;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
  }

  private static final String VALID_TRANSFER_BODY =
      """
      {
        "items": [
          {
            "supplyVariantId": 1,
            "quantity": 5.000
          }
        ]
      }
      """;

  // ---------------------------------------------------------------------------
  // E-I-07: shouldReturn200_whenTransferIsSuccessful
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_whenTransferIsSuccessful() throws Exception {
    InventoryMovement movement =
        InventoryMovement.builder()
            .id(100L)
            .supplyVariantId(1L)
            .fromStorageLocationId(1L)
            .toStorageLocationId(2L)
            .quantity(new BigDecimal("5.000"))
            .movementType(MovementType.TRANSFER)
            .createdAt(LocalDateTime.now())
            .build();

    when(transferInventoryUseCase.transferToKitchen(anyList())).thenReturn(List.of(movement));

    mockMvc
        .perform(
            post("/api/v1/inventory/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_TRANSFER_BODY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(100))
        .andExpect(jsonPath("$[0].supplyVariantId").value(1))
        .andExpect(jsonPath("$[0].quantity").value(5.0))
        .andExpect(jsonPath("$[0].movementType").value("TRANSFER"));
  }

  // ---------------------------------------------------------------------------
  // E-I-08: shouldReturn400_whenInsufficientStockInBodega
  // TransferInventoryService lanza InsufficientStockException → GlobalExceptionHandler → 409
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn400_whenInsufficientStockInBodega() throws Exception {
    when(transferInventoryUseCase.transferToKitchen(anyList()))
        .thenThrow(
            new InsufficientStockException(1L, new BigDecimal("5.000"), new BigDecimal("0.000")));

    mockMvc
        .perform(
            post("/api/v1/inventory/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_TRANSFER_BODY))
        .andExpect(status().isConflict())
        .andExpect(
            jsonPath("$.message")
                .value("Insufficient stock for variant 1: required=5.000, available=0.000"));
  }
}
