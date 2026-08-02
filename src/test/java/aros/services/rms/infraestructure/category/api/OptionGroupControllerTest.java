/* (C) 2026 */

package aros.services.rms.infraestructure.category.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.category.domain.OptionGroup;
import aros.services.rms.core.category.port.input.OptionGroupUseCase;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** Endpoint tests for OptionGroupController. */
@ExtendWith(MockitoExtension.class)
class OptionGroupControllerTest {

  @Mock private OptionGroupUseCase optionGroupUseCase;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new OptionGroupController(optionGroupUseCase)).build();
  }

  // ---------------------------------------------------------------------------
  // Happy path: search returns matching option groups
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_withOptionCategoriesFilteredBySearch() throws Exception {
    OptionGroup cat =
        OptionGroup.builder().id(1L).name("Tamaño").description("Size options").build();
    when(optionGroupUseCase.findByNameContainingIgnoreCase(eq("tamaño"))).thenReturn(List.of(cat));
    when(optionGroupUseCase.loadSelectionTypesByIds(any())).thenReturn(Map.of(1L, "SINGLE_CHOICE"));
    when(optionGroupUseCase.loadProductIdsByOptionGroupIds(any())).thenReturn(Map.of());

    mockMvc
        .perform(get("/api/v1/option-groups").param("search", "tamaño"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].name").value("Tamaño"))
        .andExpect(jsonPath("$[0].selectionType").value("SINGLE_CHOICE"));
  }

  // ---------------------------------------------------------------------------
  // Empty search: returns all option groups
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_withAllOptionCategories_whenSearchIsBlank() throws Exception {
    OptionGroup cat =
        OptionGroup.builder().id(1L).name("Sizes").description("Size options").build();
    when(optionGroupUseCase.findAll()).thenReturn(List.of(cat));
    when(optionGroupUseCase.loadSelectionTypesByIds(any())).thenReturn(Map.of(1L, "SINGLE_CHOICE"));
    when(optionGroupUseCase.loadProductIdsByOptionGroupIds(any())).thenReturn(Map.of());

    mockMvc
        .perform(get("/api/v1/option-groups").param("search", ""))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].name").value("Sizes"));
  }

  // ---------------------------------------------------------------------------
  // No matches: returns empty list
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_withEmptyList_whenSearchHasNoMatches() throws Exception {
    when(optionGroupUseCase.findByNameContainingIgnoreCase(eq("nonexistent")))
        .thenReturn(List.of());

    mockMvc
        .perform(get("/api/v1/option-groups").param("search", "nonexistent"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));
  }

  // ---------------------------------------------------------------------------
  // No search param: returns all option groups
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_withAllOptionCategories_whenNoSearchParam() throws Exception {
    OptionGroup cat =
        OptionGroup.builder().id(1L).name("Sizes").description("Size options").build();
    when(optionGroupUseCase.findAll()).thenReturn(List.of(cat));
    when(optionGroupUseCase.loadSelectionTypesByIds(any())).thenReturn(Map.of(1L, "SINGLE_CHOICE"));
    when(optionGroupUseCase.loadProductIdsByOptionGroupIds(any())).thenReturn(Map.of());

    mockMvc
        .perform(get("/api/v1/option-groups"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].name").value("Sizes"));
  }

  // ---------------------------------------------------------------------------
  // productId filter: returns only groups attached to the product
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_withOptionGroupsFilteredByProductId() throws Exception {
    OptionGroup g1 =
        OptionGroup.builder().id(1L).name("Proteína").description("Protein choices").build();
    OptionGroup g2 =
        OptionGroup.builder().id(2L).name("Queso").description("Cheese choices").build();
    when(optionGroupUseCase.findByProductId(5L)).thenReturn(List.of(g1, g2));
    when(optionGroupUseCase.loadSelectionTypesByIds(any()))
        .thenReturn(Map.of(1L, "SINGLE_CHOICE", 2L, "MULTI_CHOICE"));
    when(optionGroupUseCase.loadProductIdsByOptionGroupIds(any()))
        .thenReturn(Map.of(1L, List.of(5L), 2L, List.of(5L, 6L)));

    mockMvc
        .perform(get("/api/v1/option-groups").param("productId", "5"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].name").value("Proteína"))
        .andExpect(jsonPath("$[1].name").value("Queso"));
  }

  // ---------------------------------------------------------------------------
  // productIds populated in response
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturnOptionGroupResponse_withProductIds() throws Exception {
    OptionGroup cat =
        OptionGroup.builder().id(1L).name("Tamaño").description("Size options").build();
    when(optionGroupUseCase.findByNameContainingIgnoreCase(eq("tamaño"))).thenReturn(List.of(cat));
    when(optionGroupUseCase.loadSelectionTypesByIds(any())).thenReturn(Map.of(1L, "SINGLE_CHOICE"));
    when(optionGroupUseCase.loadProductIdsByOptionGroupIds(any()))
        .thenReturn(Map.of(1L, List.of(10L, 20L)));

    mockMvc
        .perform(get("/api/v1/option-groups").param("search", "tamaño"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].productIds").isArray())
        .andExpect(jsonPath("$[0].productIds.length()").value(2))
        .andExpect(jsonPath("$[0].productIds[0]").value(10))
        .andExpect(jsonPath("$[0].productIds[1]").value(20));
  }

  // ---------------------------------------------------------------------------
  // GET by ID returns enriched response
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_withOptionGroupById() throws Exception {
    OptionGroup cat =
        OptionGroup.builder().id(1L).name("Proteína").description("Protein choices").build();
    when(optionGroupUseCase.findById(1L)).thenReturn(cat);
    when(optionGroupUseCase.loadSelectionTypesByIds(any())).thenReturn(Map.of(1L, "SINGLE_CHOICE"));
    when(optionGroupUseCase.loadProductIdsByOptionGroupIds(any()))
        .thenReturn(Map.of(1L, List.of(1L, 2L)));

    mockMvc
        .perform(get("/api/v1/option-groups/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Proteína"))
        .andExpect(jsonPath("$.productIds.length()").value(2));
  }
}
