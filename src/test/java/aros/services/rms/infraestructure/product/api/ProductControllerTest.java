/* (C) 2026 */

package aros.services.rms.infraestructure.product.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.category.domain.Category;
import aros.services.rms.core.category.port.input.OptionGroupUseCase;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.image.port.output.ImageRepositoryPort;
import aros.services.rms.core.image.port.output.StoragePort;
import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.port.input.CalculateProductCostUseCase;
import aros.services.rms.core.product.port.input.GetProductCostBreakdownUseCase;
import aros.services.rms.core.product.port.input.ProductUseCase;
import aros.services.rms.infraestructure.common.config.RestApiConfig;
import aros.services.rms.infraestructure.common.exception.GlobalExceptionHandler;
import aros.services.rms.infraestructure.image.storage.local.LocalResourceConfig;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Web MVC slice tests for {@code GET /api/v1/products} search behavior. Loads the production Page
 * serialization config ({@link RestApiConfig}) so the response shape matches what the harness and
 * other integration tests assert.
 */
@WebMvcTest(
    value = ProductController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = LocalResourceConfig.class))
@Import({
  ProductControllerTest.TestSecurityConfig.class,
  GlobalExceptionHandler.class,
  RestApiConfig.class
})
class ProductControllerTest {

  private static final Currency COP = Currency.getInstance("COP");

  @Autowired private MockMvc mockMvc;

  @MockitoBean private ProductUseCase productUseCase;
  @MockitoBean private CalculateProductCostUseCase calculateProductCostUseCase;
  @MockitoBean private GetProductCostBreakdownUseCase getProductCostBreakdownUseCase;
  @MockitoBean private ImageRepositoryPort imageRepositoryPort;
  @MockitoBean private StoragePort storagePort;
  @MockitoBean private OptionGroupUseCase optionGroupUseCase;
  @MockitoBean private JwtDecoder jwtDecoder;

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
  // Search matching by name
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_withProductsFilteredBySearchMatchingName() throws Exception {
    Product burger = product(1L, "Hamburguesa Clásica", null, "Hamburguesas");
    Pageable pageable = PageRequest.of(0, 20);
    when(productUseCase.search(eq("burger"), isNull(), eq(false), eq(false), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(burger), pageable, 1));

    mockMvc
        .perform(get("/api/v1/products").param("search", "burger").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].id").value(1))
        .andExpect(jsonPath("$.content[0].name").value("Hamburguesa Clásica"))
        .andExpect(jsonPath("$.page.totalElements").value(1));

    verify(productUseCase)
        .search(eq("burger"), isNull(), eq(false), eq(false), any(Pageable.class));
  }

  // ---------------------------------------------------------------------------
  // Search matching by description
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_withProductsFilteredBySearchMatchingDescription() throws Exception {
    Product soda =
        product(2L, "Gaseosa 350ml", "Refrescante bebida carbonatada de cola con gas", "Bebidas");
    Pageable pageable = PageRequest.of(0, 20);
    when(productUseCase.search(
            eq("carbonatada"), isNull(), eq(false), eq(false), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(soda), pageable, 1));

    mockMvc
        .perform(get("/api/v1/products").param("search", "carbonatada").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(2))
        .andExpect(jsonPath("$.content[0].name").value("Gaseosa 350ml"))
        .andExpect(jsonPath("$.page.totalElements").value(1));

    verify(productUseCase)
        .search(eq("carbonatada"), isNull(), eq(false), eq(false), any(Pageable.class));
  }

  // ---------------------------------------------------------------------------
  // Search matching by category name
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_withProductsFilteredBySearchMatchingCategoryName() throws Exception {
    Product burger = product(3L, "Hamburguesa BBQ", null, "Hamburguesas");
    Pageable pageable = PageRequest.of(0, 20);
    when(productUseCase.search(
            eq("hamburguesas"), isNull(), eq(false), eq(false), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(burger), pageable, 1));

    mockMvc
        .perform(get("/api/v1/products").param("search", "hamburguesas").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(3))
        .andExpect(jsonPath("$.content[0].categoryName").value("Hamburguesas"))
        .andExpect(jsonPath("$.page.totalElements").value(1));

    verify(productUseCase)
        .search(eq("hamburguesas"), isNull(), eq(false), eq(false), any(Pageable.class));
  }

  // ---------------------------------------------------------------------------
  // Search with no matches returns empty page
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_withEmptyPage_whenSearchHasNoMatches() throws Exception {
    Pageable pageable = PageRequest.of(0, 20);
    when(productUseCase.search(
            eq("nonexistent"), isNull(), eq(false), eq(false), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(), pageable, 0));

    mockMvc
        .perform(get("/api/v1/products").param("search", "nonexistent").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content").isEmpty())
        .andExpect(jsonPath("$.page.totalElements").value(0));

    verify(productUseCase)
        .search(eq("nonexistent"), isNull(), eq(false), eq(false), any(Pageable.class));
  }

  // ---------------------------------------------------------------------------
  // Search absent → current behavior (uses findAllActive / findAllStandard)
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_withAllActiveProducts_whenSearchIsAbsent() throws Exception {
    Product burger = product(1L, "Burger", null, "Comida");
    Product drink = product(2L, "Drink", null, "Bebidas");
    Pageable pageable = PageRequest.of(0, 20);
    when(productUseCase.findAllActive(any(Pageable.class), eq(false)))
        .thenReturn(new PageImpl<>(List.of(burger, drink), pageable, 2));

    mockMvc
        .perform(get("/api/v1/products").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.content[0].name").value("Burger"))
        .andExpect(jsonPath("$.content[1].name").value("Drink"))
        .andExpect(jsonPath("$.page.totalElements").value(2));

    verify(productUseCase).findAllActive(any(Pageable.class), eq(false));
    verify(productUseCase, never()).search(any(), any(), anyBoolean(), anyBoolean(), any());
  }

  // ---------------------------------------------------------------------------
  // Search combined with categories filter
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_withSearchCombinedWithCategoriesFilter() throws Exception {
    Product burger = product(4L, "Burger XL", null, "Hamburguesas");
    Pageable pageable = PageRequest.of(0, 20);
    when(productUseCase.search(
            eq("burger"), eq(List.of(7L)), eq(false), eq(false), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(burger), pageable, 1));

    mockMvc
        .perform(
            get("/api/v1/products").param("search", "burger").param("categories", "7").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(4))
        .andExpect(jsonPath("$.page.totalElements").value(1));

    verify(productUseCase)
        .search(eq("burger"), eq(List.of(7L)), eq(false), eq(false), any(Pageable.class));
    verify(productUseCase, never()).findByCategoryIds(any());
  }

  // ---------------------------------------------------------------------------
  // Search combined with includeInactive=true
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_withSearchCombinedWithIncludeInactive() throws Exception {
    Product burger = product(5L, "Burger", null, "Hamburguesas");
    Pageable pageable = PageRequest.of(0, 20);
    when(productUseCase.search(eq("burger"), isNull(), eq(true), eq(false), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(burger), pageable, 1));

    mockMvc
        .perform(
            get("/api/v1/products")
                .param("search", "burger")
                .param("includeInactive", "true")
                .with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(5))
        .andExpect(jsonPath("$.page.totalElements").value(1));

    verify(productUseCase).search(eq("burger"), isNull(), eq(true), eq(false), any(Pageable.class));
  }

  // ---------------------------------------------------------------------------
  // Blank search → treated as absent (current behavior)
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_withAllProducts_whenSearchIsBlank() throws Exception {
    Product burger = product(1L, "Burger", null, "Comida");
    Pageable pageable = PageRequest.of(0, 20);
    when(productUseCase.findAllActive(any(Pageable.class), eq(false)))
        .thenReturn(new PageImpl<>(List.of(burger), pageable, 1));

    mockMvc
        .perform(get("/api/v1/products").param("search", "").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1));

    verify(productUseCase).findAllActive(any(Pageable.class), eq(false));
    verify(productUseCase, never()).search(any(), any(), anyBoolean(), anyBoolean(), any());
  }

  private static Product product(Long id, String name, String description, String categoryName) {
    return Product.builder()
        .id(id)
        .name(name)
        .description(description)
        .basePrice(new Money(new BigDecimal("10.0"), COP))
        .active(true)
        .category(Category.builder().id(1L).name(categoryName).build())
        .preparationAreaId(1L)
        .build();
  }
}
