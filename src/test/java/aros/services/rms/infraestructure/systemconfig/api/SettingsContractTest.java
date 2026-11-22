/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.systemconfig.domain.CurrencySettings;
import aros.services.rms.core.systemconfig.domain.SystemConfigurationGroup;
import aros.services.rms.core.systemconfig.domain.port.input.GetBusinessProfileUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.GetCurrencySettingsUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.GetLaborSettingsUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.GetNotificationSettingsUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.GetOpeningHoursUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.GetSystemConfigurationUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.ManageConfigurationUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateBusinessProfileUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateCurrencySettingsUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateLaborSettingsUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateNotificationSettingsUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateOpeningHoursUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateSystemConfigurationUseCase;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import aros.services.rms.infraestructure.common.exception.GlobalExceptionHandler;
import aros.services.rms.infraestructure.image.storage.local.LocalResourceConfig;
import java.util.List;
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
 * Tests for {@link SettingsController} and {@link SystemConfigurationController} as the settings
 * HTTP contract (P4-SET-13, P4-SET-14, P5-H-17).
 *
 * <p><b>Feature:</b> the HTTP contract consumed by the settings UI.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>Success bodies use the {@code data} envelope and never a flat shape
 *   <li>Group lookup accepts a name as well as a numeric id
 *   <li>Unknown groups return 404 with {@code application/problem+json}
 *   <li>Entry deletion returns 204 and group creation returns 201
 *   <li>Workers receive 403 on every settings route
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Loads both settings controllers in one WebMvc slice
 *   <li>Asserts status, content type and envelope keys, not implementation details
 * </ul>
 */
@WebMvcTest(
    controllers = {SettingsController.class, SystemConfigurationController.class},
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = LocalResourceConfig.class))
@Import({SettingsContractTest.TestSecurityConfig.class, GlobalExceptionHandler.class})
class SettingsContractTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private GetBusinessProfileUseCase getBusinessProfileUseCase;
  @MockitoBean private UpdateBusinessProfileUseCase updateBusinessProfileUseCase;
  @MockitoBean private GetCurrencySettingsUseCase getCurrencySettingsUseCase;
  @MockitoBean private UpdateCurrencySettingsUseCase updateCurrencySettingsUseCase;
  @MockitoBean private GetLaborSettingsUseCase getLaborSettingsUseCase;
  @MockitoBean private UpdateLaborSettingsUseCase updateLaborSettingsUseCase;
  @MockitoBean private GetNotificationSettingsUseCase getNotificationSettingsUseCase;
  @MockitoBean private UpdateNotificationSettingsUseCase updateNotificationSettingsUseCase;
  @MockitoBean private GetOpeningHoursUseCase getOpeningHoursUseCase;
  @MockitoBean private UpdateOpeningHoursUseCase updateOpeningHoursUseCase;
  @MockitoBean private GetSystemConfigurationUseCase getSystemConfigurationUseCase;
  @MockitoBean private UpdateSystemConfigurationUseCase updateSystemConfigurationUseCase;
  @MockitoBean private ManageConfigurationUseCase manageConfigurationUseCase;
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
    when(currencyProvider.getCurrency()).thenReturn(java.util.Currency.getInstance("COP"));
  }

  @Test
  void should_wrap_settings_success_body_in_data_envelope() throws Exception {
    when(getCurrencySettingsUseCase.get())
        .thenReturn(new CurrencySettings("COP", "Colombian Peso", "$", 2));

    mockMvc
        .perform(get("/api/v1/settings/currency").with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.code").value("COP"))
        .andExpect(jsonPath("$.code").doesNotExist());
  }

  @Test
  void should_resolve_config_group_by_name() throws Exception {
    SystemConfigurationGroup group =
        SystemConfigurationGroup.builder().id(1L).name("General").sortOrder(1).build();
    when(getSystemConfigurationUseCase.findAllGroups()).thenReturn(List.of(group));
    when(getSystemConfigurationUseCase.findByGroupId(1L)).thenReturn(List.of());

    mockMvc
        .perform(get("/api/v1/config/general").with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isArray());
  }

  @Test
  void should_return_not_found_problem_for_unknown_group() throws Exception {
    when(getSystemConfigurationUseCase.findAllGroups()).thenReturn(List.of());

    mockMvc
        .perform(get("/api/v1/config/unknown").with(adminJwt()))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
  }

  @Test
  void should_return_created_for_group_creation() throws Exception {
    when(manageConfigurationUseCase.createGroup(any()))
        .thenReturn(
            SystemConfigurationGroup.builder().id(4L).name("Notificaciones").sortOrder(4).build());

    mockMvc
        .perform(
            post("/api/v1/config/groups")
                .with(adminJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Notificaciones\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.name").value("Notificaciones"));
  }

  @Test
  void should_return_no_content_for_entry_deletion() throws Exception {
    mockMvc
        .perform(delete("/api/v1/config/legacy_key").with(adminJwt()))
        .andExpect(status().isNoContent());
  }

  @Test
  void should_forbid_worker_on_settings_routes() throws Exception {
    mockMvc
        .perform(get("/api/v1/settings/currency").with(workerJwt()))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
  }
}
