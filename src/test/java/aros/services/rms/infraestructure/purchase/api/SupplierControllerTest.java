/* (C) 2026 */

package aros.services.rms.infraestructure.purchase.api;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.purchase.domain.Supplier;
import aros.services.rms.core.purchase.port.input.CreateSupplierUseCase;
import aros.services.rms.core.purchase.port.input.GetSuppliersUseCase;
import aros.services.rms.core.purchase.port.input.UpdateSupplierUseCase;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** Endpoint tests for SupplierController search behavior. */
@ExtendWith(MockitoExtension.class)
class SupplierControllerTest {

  @Mock private CreateSupplierUseCase createSupplierUseCase;
  @Mock private UpdateSupplierUseCase updateSupplierUseCase;
  @Mock private GetSuppliersUseCase getSuppliersUseCase;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(
                new SupplierController(
                    createSupplierUseCase, updateSupplierUseCase, getSuppliersUseCase))
            .build();
  }

  @Test
  void shouldReturn200_withSuppliersFilteredBySearch() throws Exception {
    Supplier supplier =
        Supplier.builder().id(1L).name("Distribuidora Mayorista").contact("3001234567").build();
    when(getSuppliersUseCase.findByNameContainingIgnoreCase("mayorista"))
        .thenReturn(List.of(supplier));

    mockMvc
        .perform(get("/api/v1/suppliers").param("search", "mayorista"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].name").value("Distribuidora Mayorista"));

    verify(getSuppliersUseCase).findByNameContainingIgnoreCase("mayorista");
  }

  @Test
  void shouldReturn200_withAllSuppliers_whenSearchIsBlank() throws Exception {
    when(getSuppliersUseCase.findAll())
        .thenReturn(
            List.of(
                Supplier.builder().id(1L).name("Supplier One").build(),
                Supplier.builder().id(2L).name("Supplier Two").build()));

    mockMvc
        .perform(get("/api/v1/suppliers").param("search", ""))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));

    verify(getSuppliersUseCase).findAll();
    verify(getSuppliersUseCase, never()).findByNameContainingIgnoreCase("");
  }

  @Test
  void shouldReturn200_withEmptyList_whenSearchHasNoMatches() throws Exception {
    when(getSuppliersUseCase.findByNameContainingIgnoreCase("nonexistent")).thenReturn(List.of());

    mockMvc
        .perform(get("/api/v1/suppliers").param("search", "nonexistent"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));
  }
}
