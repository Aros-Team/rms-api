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
  // Happy path: search returns matching option categories
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_withOptionCategoriesFilteredBySearch() throws Exception {
    OptionGroup cat =
        OptionGroup.builder().id(1L).name("Tamaño").description("Size options").build();
    when(optionGroupUseCase.findByNameContainingIgnoreCase(eq("tamaño"))).thenReturn(List.of(cat));
    when(optionGroupUseCase.loadSelectionTypesByIds(any())).thenReturn(Map.of(1L, "SINGLE_CHOICE"));

    mockMvc
        .perform(get("/api/v1/option-categories").param("search", "tamaño"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].name").value("Tamaño"))
        .andExpect(jsonPath("$[0].selectionType").value("SINGLE_CHOICE"));
  }

  // ---------------------------------------------------------------------------
  // Empty search: returns all option categories (current behavior)
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_withAllOptionCategories_whenSearchIsBlank() throws Exception {
    OptionGroup cat =
        OptionGroup.builder().id(1L).name("Sizes").description("Size options").build();
    when(optionGroupUseCase.findAll()).thenReturn(List.of(cat));
    when(optionGroupUseCase.loadSelectionTypesByIds(any())).thenReturn(Map.of(1L, "SINGLE_CHOICE"));

    mockMvc
        .perform(get("/api/v1/option-categories").param("search", ""))
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
        .perform(get("/api/v1/option-categories").param("search", "nonexistent"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));
  }

  // ---------------------------------------------------------------------------
  // No search param: returns all option categories
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_withAllOptionCategories_whenNoSearchParam() throws Exception {
    OptionGroup cat =
        OptionGroup.builder().id(1L).name("Sizes").description("Size options").build();
    when(optionGroupUseCase.findAll()).thenReturn(List.of(cat));
    when(optionGroupUseCase.loadSelectionTypesByIds(any())).thenReturn(Map.of(1L, "SINGLE_CHOICE"));

    mockMvc
        .perform(get("/api/v1/option-categories"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].name").value("Sizes"));
  }
}
