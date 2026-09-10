/* (C) 2026 */

package aros.services.rms.infraestructure.purchase.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.purchase.domain.PurchaseOrder;
import aros.services.rms.core.purchase.port.input.GetPurchaseHistoryUseCase;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import aros.services.rms.infraestructure.purchase.config.RegisterPurchaseOrderService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Tests for {@link PurchaseOrderController}.
 *
 * <p><b>Feature:</b> Purchase order REST endpoint
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>should Return200 with Purchase Orders Filtered By Search
 *   <li>should Return200 with All Purchase Orders when Search Is Blank
 *   <li>should Return200 with Empty List when Search Has No Matches
 *   <li>should Prioritize Search over Date Range
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Uses MockMvc to simulate HTTP requests and verify responses
 *   <li>Mocks service-layer dependencies via Mockito
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class PurchaseOrderControllerTest {

  @Mock private RegisterPurchaseOrderService registerPurchaseOrderService;
  @Mock private GetPurchaseHistoryUseCase getPurchaseHistoryUseCase;
  @Mock private CurrencyProvider currencyProvider;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(
                new PurchaseOrderController(
                    registerPurchaseOrderService, getPurchaseHistoryUseCase, currencyProvider))
            .build();
  }

  @Test
  void shouldReturn200_withPurchaseOrdersFilteredBySearch() throws Exception {
    PurchaseOrder order = purchaseOrder(10L, 2L, "Fresh produce delivery");
    when(getPurchaseHistoryUseCase.findBySearch("produce")).thenReturn(List.of(order));

    mockMvc
        .perform(get("/api/v1/purchases").param("search", "produce"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(10))
        .andExpect(jsonPath("$[0].notes").value("Fresh produce delivery"));

    verify(getPurchaseHistoryUseCase).findBySearch("produce");
  }

  @Test
  void shouldReturn200_withAllPurchaseOrders_whenSearchIsBlank() throws Exception {
    when(getPurchaseHistoryUseCase.findAll())
        .thenReturn(
            List.of(purchaseOrder(10L, 2L, "First order"), purchaseOrder(11L, 3L, "Second order")));

    mockMvc
        .perform(get("/api/v1/purchases").param("search", ""))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));

    verify(getPurchaseHistoryUseCase).findAll();
    verify(getPurchaseHistoryUseCase, never()).findBySearch(anyString());
  }

  @Test
  void shouldReturn200_withEmptyList_whenSearchHasNoMatches() throws Exception {
    when(getPurchaseHistoryUseCase.findBySearch("nonexistent")).thenReturn(List.of());

    mockMvc
        .perform(get("/api/v1/purchases").param("search", "nonexistent"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void shouldPrioritizeSearch_overDateRange() throws Exception {
    PurchaseOrder order = purchaseOrder(10L, 2L, "Fresh produce delivery");
    when(getPurchaseHistoryUseCase.findBySearch("produce")).thenReturn(List.of(order));

    mockMvc
        .perform(
            get("/api/v1/purchases")
                .param("search", "produce")
                .param("from", "2026-01-01")
                .param("to", "2026-01-31"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(10));

    verify(getPurchaseHistoryUseCase).findBySearch("produce");
    verify(getPurchaseHistoryUseCase, never()).findByDateRange(any(), any());
  }

  @Test
  void shouldPrioritizeSupplierId_overSearch() throws Exception {
    PurchaseOrder order = purchaseOrder(12L, 7L, "Supplier-specific order");
    when(getPurchaseHistoryUseCase.findBySupplierId(7L)).thenReturn(List.of(order));

    mockMvc
        .perform(get("/api/v1/purchases").param("supplierId", "7").param("search", "ignored"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(12))
        .andExpect(jsonPath("$[0].supplierId").value(7));

    verify(getPurchaseHistoryUseCase).findBySupplierId(7L);
    verify(getPurchaseHistoryUseCase, never()).findBySearch(anyString());
  }

  private static PurchaseOrder purchaseOrder(Long id, Long supplierId, String notes) {
    return PurchaseOrder.builder().id(id).supplierId(supplierId).notes(notes).build();
  }
}
