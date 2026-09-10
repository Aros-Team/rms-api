/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.systemconfig.domain.SystemConfiguration;
import aros.services.rms.core.systemconfig.domain.SystemConfigurationGroup;
import aros.services.rms.core.systemconfig.domain.port.input.GetSystemConfigurationUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateSystemConfigurationUseCase;
import aros.services.rms.infraestructure.common.exception.GlobalExceptionHandler;
import aros.services.rms.infraestructure.image.storage.local.LocalResourceConfig;
import java.time.Instant;
import java.util.List;
import java.util.Map;
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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests for {@link SystemConfigurationController}.
 *
 * <p><b>Feature:</b> System configuration REST endpoint
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>should get all groups with entries
 *   <li>should get entries by group
 *   <li>should update configurations
 *   <li>should return unauthorized without auth
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Uses MockMvc to simulate HTTP requests and verify responses
 *   <li>Mocks service-layer dependencies via Mockito
 * </ul>
 */
@WebMvcTest(
    value = SystemConfigurationController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = LocalResourceConfig.class))
@Import({SystemConfigurationControllerTest.TestSecurityConfig.class, GlobalExceptionHandler.class})
class SystemConfigurationControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private GetSystemConfigurationUseCase getSystemConfigurationUseCase;
  @MockitoBean private UpdateSystemConfigurationUseCase updateSystemConfigurationUseCase;
  @MockitoBean private JwtDecoder jwtDecoder;

  @org.springframework.boot.test.context.TestConfiguration
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

  private static Jwt createTestJwt() {
    return new Jwt(
        "token",
        Instant.now(),
        Instant.now().plusSeconds(3600),
        Map.of("alg", "RS256"),
        Map.of("sub", "admin@test.com"));
  }

  @BeforeEach
  void setUp() {
    when(jwtDecoder.decode(anyString())).thenReturn(createTestJwt());
  }

  // ---------------------------------------------------------------------------
  // UC-01: should_get_all_groups_with_entries
  // ---------------------------------------------------------------------------

  @Test
  void should_get_all_groups_with_entries() throws Exception {
    SystemConfigurationGroup group =
        SystemConfigurationGroup.builder()
            .id(1L)
            .name("General")
            .description("Moneda, zona horaria, pais")
            .sortOrder(1)
            .build();

    SystemConfiguration config =
        SystemConfiguration.builder()
            .id(1L)
            .key("default_currency")
            .value("COP")
            .groupId(1L)
            .sortOrder(1)
            .build();

    when(getSystemConfigurationUseCase.findAllGroups()).thenReturn(List.of(group));
    when(getSystemConfigurationUseCase.findByGroupId(1L)).thenReturn(List.of(config));

    mockMvc
        .perform(
            get("/api/v1/config")
                .header("Authorization", "Bearer test-token")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].name").value("General"))
        .andExpect(jsonPath("$[0].entries.length()").value(1))
        .andExpect(jsonPath("$[0].entries[0].key").value("default_currency"))
        .andExpect(jsonPath("$[0].entries[0].value").value("COP"));
  }

  // ---------------------------------------------------------------------------
  // UC-02: should_get_entries_by_group
  // ---------------------------------------------------------------------------

  @Test
  void should_get_entries_by_group() throws Exception {
    List<SystemConfiguration> configs =
        List.of(
            SystemConfiguration.builder().id(1L).key("currency").value("COP").groupId(1L).build(),
            SystemConfiguration.builder().id(2L).key("timezone").value("COT").groupId(1L).build());

    when(getSystemConfigurationUseCase.findByGroupId(1L)).thenReturn(configs);

    mockMvc
        .perform(
            get("/api/v1/config/1")
                .header("Authorization", "Bearer test-token")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].key").value("currency"))
        .andExpect(jsonPath("$[1].key").value("timezone"));
  }

  // ---------------------------------------------------------------------------
  // UC-03: should_update_configurations
  // ---------------------------------------------------------------------------

  @Test
  void should_update_configurations() throws Exception {
    String requestBody = "[{\"key\": \"currency\", \"value\": \"USD\"}]";

    List<SystemConfiguration> saved =
        List.of(SystemConfiguration.builder().id(1L).key("currency").value("USD").build());

    when(updateSystemConfigurationUseCase.saveAll(any())).thenReturn(saved);

    mockMvc
        .perform(
            put("/api/v1/config")
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].key").value("currency"))
        .andExpect(jsonPath("$[0].value").value("USD"));
  }

  // ---------------------------------------------------------------------------
  // UC-04: should_return_unauthorized_without_auth
  // ---------------------------------------------------------------------------

  @Test
  void should_return_unauthorized_without_auth() throws Exception {
    mockMvc
        .perform(get("/api/v1/config").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }
}
