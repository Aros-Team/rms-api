/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.systemconfig.domain.BusinessProfile;
import aros.services.rms.core.systemconfig.domain.CurrencySettings;
import aros.services.rms.core.systemconfig.domain.LaborCostMode;
import aros.services.rms.core.systemconfig.domain.LaborSettings;
import aros.services.rms.core.systemconfig.domain.NotificationSettings;
import aros.services.rms.core.systemconfig.domain.exception.InvalidConfigurationException;
import aros.services.rms.core.systemconfig.domain.port.input.GetBusinessProfileUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.GetCurrencySettingsUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.GetLaborSettingsUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.GetNotificationSettingsUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateBusinessProfileUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateCurrencySettingsUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateLaborSettingsUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateNotificationSettingsUseCase;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import aros.services.rms.infraestructure.common.exception.GlobalExceptionHandler;
import aros.services.rms.infraestructure.image.storage.local.LocalResourceConfig;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests for {@link SettingsController}.
 *
 * <p><b>Feature:</b> typed business, currency, labor and notification settings.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>Admin reads and writes each settings section inside the {@code data} envelope
 *   <li>Invalid values return an RFC 7807 400 problem without touching the service
 *   <li>Workers are rejected with 403 and anonymous callers with 401
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Loads the controller in a WebMvc slice with method security enabled
 *   <li>Mocks the typed use cases and asserts status plus meaningful body fields
 * </ul>
 */
@WebMvcTest(
    value = SettingsController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = LocalResourceConfig.class))
@Import({SettingsControllerTest.TestSecurityConfig.class, GlobalExceptionHandler.class})
class SettingsControllerTest {

  private static final Currency COP = Currency.getInstance("COP");

  @Autowired private MockMvc mockMvc;

  @MockitoBean private GetBusinessProfileUseCase getBusinessProfileUseCase;
  @MockitoBean private UpdateBusinessProfileUseCase updateBusinessProfileUseCase;
  @MockitoBean private GetCurrencySettingsUseCase getCurrencySettingsUseCase;
  @MockitoBean private UpdateCurrencySettingsUseCase updateCurrencySettingsUseCase;
  @MockitoBean private GetLaborSettingsUseCase getLaborSettingsUseCase;
  @MockitoBean private UpdateLaborSettingsUseCase updateLaborSettingsUseCase;
  @MockitoBean private GetNotificationSettingsUseCase getNotificationSettingsUseCase;
  @MockitoBean private UpdateNotificationSettingsUseCase updateNotificationSettingsUseCase;
  @MockitoBean private CurrencyProvider currencyProvider;
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

  private static JwtRequestPostProcessor adminJwt() {
    return jwt()
        .jwt(builder -> builder.subject("admin@test.com").claim("role", "ADMIN"))
        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
  }

  private static JwtRequestPostProcessor workerJwt() {
    return jwt()
        .jwt(builder -> builder.subject("worker@test.com").claim("role", "WORKER"))
        .authorities(new SimpleGrantedAuthority("ROLE_USER"));
  }

  @BeforeEach
  void setUp() {
    when(currencyProvider.getCurrency()).thenReturn(COP);
  }

  @Test
  void should_return_business_profile_in_envelope() throws Exception {
    when(getBusinessProfileUseCase.get())
        .thenReturn(
            BusinessProfile.builder()
                .restaurantName("Rincón del Sabor")
                .legalName("Rincón S.A.S.")
                .taxId("900123456-7")
                .address("Calle 12")
                .phone("3001234567")
                .email("contacto@rms.local")
                .timezone("America/Bogota")
                .locale("es-CO")
                .build());

    mockMvc
        .perform(get("/api/v1/settings/business").with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.restaurantName").value("Rincón del Sabor"))
        .andExpect(jsonPath("$.data.timezone").value("America/Bogota"))
        .andExpect(jsonPath("$.data.locale").value("es-CO"));
  }

  @Test
  void should_update_business_profile() throws Exception {
    String body =
        """
        {
          "restaurantName": "Rincón del Sabor",
          "legalName": "Rincón S.A.S.",
          "taxId": "900123456-7",
          "address": "Calle 12",
          "phone": "3001234567",
          "email": "contacto@rms.local",
          "timezone": "America/Bogota",
          "locale": "es-CO"
        }
        """;
    when(updateBusinessProfileUseCase.update(any()))
        .thenAnswer(
            invocation -> {
              BusinessProfile profile = invocation.getArgument(0);
              return profile;
            });

    mockMvc
        .perform(
            put("/api/v1/settings/business")
                .with(adminJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.taxId").value("900123456-7"));
  }

  @Test
  void should_reject_blank_restaurant_name_with_problem_detail() throws Exception {
    String body =
        """
        {"restaurantName": "", "timezone": "America/Bogota", "locale": "es-CO"}
        """;

    mockMvc
        .perform(
            put("/api/v1/settings/business")
                .with(adminJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
  }

  @Test
  void should_map_invalid_timezone_to_problem_detail() throws Exception {
    when(updateBusinessProfileUseCase.update(any()))
        .thenThrow(new InvalidConfigurationException("Invalid IANA timezone: Mars/Olympus"));
    String body =
        """
        {"restaurantName": "RMS", "timezone": "Mars/Olympus", "locale": "es-CO"}
        """;

    mockMvc
        .perform(
            put("/api/v1/settings/business")
                .with(adminJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.detail").value("Invalid IANA timezone: Mars/Olympus"));
  }

  @Test
  void should_return_currency_settings() throws Exception {
    when(getCurrencySettingsUseCase.get())
        .thenReturn(new CurrencySettings("COP", "Colombian Peso", "$", 2));

    mockMvc
        .perform(get("/api/v1/settings/currency").with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.code").value("COP"))
        .andExpect(jsonPath("$.data.defaultFractionDigits").value(2));
  }

  @Test
  void should_update_currency_code() throws Exception {
    when(updateCurrencySettingsUseCase.update("usd"))
        .thenReturn(new CurrencySettings("USD", "US Dollar", "$", 2));

    mockMvc
        .perform(
            put("/api/v1/settings/currency")
                .with(adminJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"usd\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.code").value("USD"));
  }

  @Test
  void should_reject_non_iso_currency_code_at_validation() throws Exception {
    mockMvc
        .perform(
            put("/api/v1/settings/currency")
                .with(adminJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"colombian-peso\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
  }

  @Test
  void should_return_labor_settings_with_money_rate() throws Exception {
    when(getLaborSettingsUseCase.get())
        .thenReturn(
            new LaborSettings(
                LaborCostMode.STANDARD, new Money(new BigDecimal("12000.00"), COP), 160));

    mockMvc
        .perform(get("/api/v1/settings/labor").with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.mode").value("STANDARD"))
        .andExpect(jsonPath("$.data.standardHourlyRate.amount").value("12000.00"))
        .andExpect(jsonPath("$.data.standardHourlyRate.currency").value("COP"))
        .andExpect(jsonPath("$.data.defaultExpectedHoursPerMonth").value(160));
  }

  @Test
  void should_update_labor_settings() throws Exception {
    when(updateLaborSettingsUseCase.update(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    mockMvc
        .perform(
            put("/api/v1/settings/labor")
                .with(adminJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"mode":"REAL","standardHourlyRate":15000.00,
                     "defaultExpectedHoursPerMonth":170}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.mode").value("REAL"))
        .andExpect(jsonPath("$.data.standardHourlyRate.amount").value("15000.00"));
  }

  @Test
  void should_reject_negative_labor_rate() throws Exception {
    mockMvc
        .perform(
            put("/api/v1/settings/labor")
                .with(adminJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"mode":"REAL","standardHourlyRate":-1,
                     "defaultExpectedHoursPerMonth":160}
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
  }

  @Test
  void should_return_notification_settings() throws Exception {
    when(getNotificationSettingsUseCase.get())
        .thenReturn(new NotificationSettings(true, false, true, true, false));

    mockMvc
        .perform(get("/api/v1/settings/notifications").with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.kitchenSoundEnabled").value(true))
        .andExpect(jsonPath("$.data.kitchenHapticsEnabled").value(false))
        .andExpect(jsonPath("$.data.lowStockAlertsEnabled").value(true));
  }

  @Test
  void should_update_notification_settings() throws Exception {
    when(updateNotificationSettingsUseCase.update(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    mockMvc
        .perform(
            put("/api/v1/settings/notifications")
                .with(adminJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"kitchenSoundEnabled":false,"kitchenHapticsEnabled":true,
                     "lowStockAlertsEnabled":false,"orderAlertsEnabled":true,
                     "dailySummaryEnabled":true}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.kitchenHapticsEnabled").value(true))
        .andExpect(jsonPath("$.data.dailySummaryEnabled").value(true));
  }

  @Test
  void should_forbid_worker_from_reading_settings() throws Exception {
    mockMvc
        .perform(get("/api/v1/settings/business").with(workerJwt()))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
  }

  @Test
  void should_reject_anonymous_caller() throws Exception {
    mockMvc.perform(get("/api/v1/settings/business")).andExpect(status().isUnauthorized());
  }
}
