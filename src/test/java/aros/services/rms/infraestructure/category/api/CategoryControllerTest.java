/* (C) 2026 */

package aros.services.rms.infraestructure.category.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.category.domain.Category;
import aros.services.rms.core.category.port.input.CategoryUseCase;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Tests for {@link CategoryController}.
 *
 * <p><b>Feature:</b> Category REST endpoint
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>should Return200 with Categories Filtered By Search
 *   <li>should Return200 with All Categories when Search Is Blank
 *   <li>should Return200 with Empty List when Search Has No Matches
 *   <li>should Return200 with All Categories when No Search Param
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
class CategoryControllerTest {

  @Mock private CategoryUseCase categoryUseCase;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(new CategoryController(categoryUseCase)).build();
  }

  // ---------------------------------------------------------------------------
  // Happy path: search returns matching categories
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_withCategoriesFilteredBySearch() throws Exception {
    when(categoryUseCase.findByNameContainingIgnoreCase(eq("tamaño")))
        .thenReturn(
            List.of(
                Category.builder().id(1L).name("Tamaño grande").build(),
                Category.builder().id(2L).name("Tamaño mediano").build()));

    mockMvc
        .perform(get("/api/v1/categories").param("search", "tamaño"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].name").value("Tamaño grande"))
        .andExpect(jsonPath("$[1].name").value("Tamaño mediano"));
  }

  // ---------------------------------------------------------------------------
  // Empty search: returns all categories (current behavior)
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_withAllCategories_whenSearchIsBlank() throws Exception {
    when(categoryUseCase.findAll())
        .thenReturn(
            List.of(
                Category.builder().id(1L).name("Entradas").build(),
                Category.builder().id(2L).name("Bebidas").build()));

    mockMvc
        .perform(get("/api/v1/categories").param("search", ""))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].name").value("Entradas"))
        .andExpect(jsonPath("$[1].name").value("Bebidas"));
  }

  // ---------------------------------------------------------------------------
  // No matches: returns empty list
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_withEmptyList_whenSearchHasNoMatches() throws Exception {
    when(categoryUseCase.findByNameContainingIgnoreCase(eq("nonexistent"))).thenReturn(List.of());

    mockMvc
        .perform(get("/api/v1/categories").param("search", "nonexistent"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));
  }

  // ---------------------------------------------------------------------------
  // No search param: returns all categories
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_withAllCategories_whenNoSearchParam() throws Exception {
    when(categoryUseCase.findAll())
        .thenReturn(
            List.of(
                Category.builder().id(1L).name("Entradas").build(),
                Category.builder().id(2L).name("Bebidas").build()));

    mockMvc
        .perform(get("/api/v1/categories"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].name").value("Entradas"))
        .andExpect(jsonPath("$[1].name").value("Bebidas"));
  }
}
