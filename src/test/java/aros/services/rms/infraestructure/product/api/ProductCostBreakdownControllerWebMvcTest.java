/* (C) 2026 */

package aros.services.rms.infraestructure.product.api;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.product.application.exception.ProductNotFoundException;
import aros.services.rms.core.product.domain.ProductCostBreakdown;
import aros.services.rms.core.product.domain.ProductCostBreakdown.CategoryCost;
import aros.services.rms.core.product.domain.ProductCostBreakdown.OptionCost;
import aros.services.rms.core.product.port.input.GetProductCostBreakdownUseCase;
import aros.services.rms.infraestructure.image.storage.local.LocalResourceConfig;
import aros.services.rms.infraestructure.product.api.mapper.ProductCostBreakdownResponseMapper;
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
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Web MVC tests for GET /api/v1/products/{id}/cost-breakdown. */
@WebMvcTest(
    value = ProductCostBreakdownController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = LocalResourceConfig.class))
@Import({
  ProductCostBreakdownResponseMapper.class,
  ProductCostBreakdownControllerWebMvcTest.TestSecurityConfig.class
})
class ProductCostBreakdownControllerWebMvcTest {

  private static final String URL = "/api/v1/products/42/cost-breakdown";
  private static final Currency COP = Currency.getInstance("COP");

  @Autowired private MockMvc mockMvc;

  @MockitoBean private GetProductCostBreakdownUseCase getProductCostBreakdownUseCase;
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

  @Test
  void should_return_cost_breakdown_when_product_exists() throws Exception {
    ProductCostBreakdown breakdown =
        new ProductCostBreakdown(
            42L,
            "Burger",
            money("10"),
            List.of(
                new OptionCost(
                    7L, "Pollo", money("6"), money("2"), 3L, "Proteína", "SINGLE_CHOICE")),
            List.of(
                new CategoryCost(
                    3L, "Proteína", "SINGLE_CHOICE", money("4"), money("5"), money("1"))),
            money("1"),
            money("11"));
    when(getProductCostBreakdownUseCase.execute(42L)).thenReturn(breakdown);

    mockMvc
        .perform(get(URL).with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.productId").value(42))
        .andExpect(jsonPath("$.name").value("Burger"))
        .andExpect(jsonPath("$.baseCost.amount").value(10.0))
        .andExpect(jsonPath("$.baseCost.currency").value("COP"))
        .andExpect(jsonPath("$.options[0].optionId").value(7))
        .andExpect(jsonPath("$.options[0].cost.amount").value(6.0))
        .andExpect(jsonPath("$.options[0].extraPrice.amount").value(2.0))
        .andExpect(jsonPath("$.categories[0].projectedContribution.amount").value(1.0))
        .andExpect(jsonPath("$.projectedOptionCost.amount").value(1.0))
        .andExpect(jsonPath("$.projectedEffectiveCost.amount").value(11.0));
  }

  @Test
  void should_return_not_found_when_product_does_not_exist() throws Exception {
    when(getProductCostBreakdownUseCase.execute(42L)).thenThrow(new ProductNotFoundException(42L));

    mockMvc
        .perform(get(URL).with(jwt()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message").value("Product not found: 42"));
  }

  private static Money money(String amount) {
    return new Money(new BigDecimal(amount), COP);
  }
}
