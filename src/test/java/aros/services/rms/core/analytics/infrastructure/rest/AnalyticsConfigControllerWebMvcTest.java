/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.analytics.domain.AnalyticsConfig;
import aros.services.rms.core.analytics.domain.port.in.GetAnalyticsConfigUseCase;
import aros.services.rms.core.analytics.domain.port.in.UpdateAnalyticsConfigUseCase;
import aros.services.rms.core.analytics.domain.port.in.UpdateAnalyticsConfigUseCase.UpdateAnalyticsConfigCommand;
import aros.services.rms.core.analytics.infrastructure.rest.dto.AnalyticsConfigResponse;
import aros.services.rms.core.analytics.infrastructure.rest.mapper.AnalyticsConfigResponseMapper;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserEmail;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.domain.UserRole;
import aros.services.rms.core.user.domain.UserStatus;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import aros.services.rms.infraestructure.image.storage.local.LocalResourceConfig;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Web MVC slice tests for {@link AnalyticsConfigController}. Covers auth + validation rules for GET
 * and PATCH /api/v1/analytics/config.
 */
@WebMvcTest(
    value = AnalyticsConfigController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = LocalResourceConfig.class))
@Import(AnalyticsConfigControllerWebMvcTest.TestSecurityConfig.class)
class AnalyticsConfigControllerWebMvcTest {

  private static final String URL = "/api/v1/analytics/config";
  private static final String ADMIN_SUBJECT = "admin@test.com";
  private static final Long ADMIN_ID = 7L;

  @Autowired private MockMvc mockMvc;

  @MockitoBean private GetAnalyticsConfigUseCase getAnalyticsConfigUseCase;
  @MockitoBean private UpdateAnalyticsConfigUseCase updateAnalyticsConfigUseCase;
  @MockitoBean private AnalyticsConfigResponseMapper analyticsConfigResponseMapper;
  @MockitoBean private UserRepositoryPort userRepositoryPort;
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
  // G-01: GET returns 200 + JSON shape when caller has ROLE_ADMIN
  // ---------------------------------------------------------------------------

  @Test
  void should_allow_admin_to_get_config() throws Exception {
    AnalyticsConfig config = defaultConfig();
    when(getAnalyticsConfigUseCase.get()).thenReturn(config);
    when(analyticsConfigResponseMapper.toResponse(config)).thenReturn(toResponse(config));

    mockMvc
        .perform(get(URL).with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.defaultOpen").value("11:00:00"))
        .andExpect(jsonPath("$.defaultClose").value("23:00:00"))
        .andExpect(jsonPath("$.foodCostDeviationPp").value(2.00))
        .andExpect(jsonPath("$.laborCostDeviationPp").value(3.00))
        .andExpect(jsonPath("$.salesDropYoyPct").value(10.00));
  }

  // ---------------------------------------------------------------------------
  // G-02: GET returns 401 when unauthenticated
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_unauthenticated_get() throws Exception {
    mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());
  }

  // ---------------------------------------------------------------------------
  // G-03: GET returns 403 when authenticated but not ROLE_ADMIN
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_non_admin_get() throws Exception {
    mockMvc.perform(get(URL).with(workerJwt())).andExpect(status().isForbidden());
  }

  // ---------------------------------------------------------------------------
  // P-01: PATCH returns 200 when admin and body is valid
  // ---------------------------------------------------------------------------

  @Test
  void should_allow_admin_to_update_config() throws Exception {
    AnalyticsConfig updated = defaultConfig();
    when(updateAnalyticsConfigUseCase.update(any())).thenReturn(updated);
    when(analyticsConfigResponseMapper.toCommand(any())).thenReturn(validCommand());
    when(analyticsConfigResponseMapper.toResponse(updated)).thenReturn(toResponse(updated));
    when(userRepositoryPort.findByEmail(ADMIN_SUBJECT)).thenReturn(Optional.of(adminUser()));

    String body =
        """
        {
          "defaultOpen": "11:00:00",
          "defaultClose": "23:00:00",
          "lunchStart": "11:00:00",
          "lunchEnd": "15:00:00",
          "dinnerStart": "18:00:00",
          "dinnerEnd": "23:00:00",
          "foodCostDeviationPp": 2.00,
          "laborCostDeviationPp": 3.00,
          "salesDropYoyPct": 10.00
        }
        """;

    mockMvc
        .perform(patch(URL).with(adminJwt()).contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1));
  }

  // ---------------------------------------------------------------------------
  // P-02: PATCH returns 400 when thresholds violate @DecimalMin
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_update_with_negative_threshold() throws Exception {
    String body =
        """
        {
          "defaultOpen": "11:00:00",
          "defaultClose": "23:00:00",
          "lunchStart": "11:00:00",
          "lunchEnd": "15:00:00",
          "dinnerStart": "18:00:00",
          "dinnerEnd": "23:00:00",
          "foodCostDeviationPp": -1.00,
          "laborCostDeviationPp": 3.00,
          "salesDropYoyPct": 10.00
        }
        """;

    mockMvc
        .perform(patch(URL).with(adminJwt()).contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());
  }

  // ---------------------------------------------------------------------------
  // P-03: PATCH returns 403 when caller is not ROLE_ADMIN
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_non_admin_update() throws Exception {
    String body =
        """
        {
          "defaultOpen": "11:00:00",
          "defaultClose": "23:00:00",
          "lunchStart": "11:00:00",
          "lunchEnd": "15:00:00",
          "dinnerStart": "18:00:00",
          "dinnerEnd": "23:00:00",
          "foodCostDeviationPp": 2.00,
          "laborCostDeviationPp": 3.00,
          "salesDropYoyPct": 10.00
        }
        """;

    mockMvc
        .perform(patch(URL).with(workerJwt()).contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isForbidden());
  }

  // ---------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------

  private static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor adminJwt() {
    return jwt()
        .jwt(builder -> builder.subject(ADMIN_SUBJECT).claim("role", "ADMIN"))
        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
  }

  private static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor workerJwt() {
    return jwt()
        .jwt(builder -> builder.subject("worker@test.com").claim("role", "WORKER"))
        .authorities(new SimpleGrantedAuthority("ROLE_USER"));
  }

  private static AnalyticsConfig defaultConfig() {
    return new AnalyticsConfig(
        1,
        LocalTime.of(11, 0),
        LocalTime.of(23, 0),
        LocalTime.of(11, 0),
        LocalTime.of(15, 0),
        LocalTime.of(18, 0),
        LocalTime.of(23, 0),
        new BigDecimal("2.00"),
        new BigDecimal("3.00"),
        new BigDecimal("10.00"),
        LocalDateTime.parse("2026-07-17T00:00:00"),
        ADMIN_ID);
  }

  private static User adminUser() {
    return new User(
        UserId.of(ADMIN_ID),
        "1234567890",
        "Admin",
        new UserEmail(ADMIN_SUBJECT),
        "encoded",
        "Address",
        "555-0100",
        UserRole.ADMIN,
        UserStatus.ACTIVE,
        List.of());
  }

  private static AnalyticsConfigResponse toResponse(AnalyticsConfig config) {
    return AnalyticsConfigResponse.builder()
        .id(config.id())
        .defaultOpen(config.defaultOpen())
        .defaultClose(config.defaultClose())
        .lunchStart(config.lunchStart())
        .lunchEnd(config.lunchEnd())
        .dinnerStart(config.dinnerStart())
        .dinnerEnd(config.dinnerEnd())
        .foodCostDeviationPp(config.foodCostDeviationPp())
        .laborCostDeviationPp(config.laborCostDeviationPp())
        .salesDropYoyPct(config.salesDropYoyPct())
        .updatedAt(config.updatedAt())
        .updatedBy(config.updatedBy())
        .build();
  }

  private static UpdateAnalyticsConfigCommand validCommand() {
    return new UpdateAnalyticsConfigCommand(
        LocalTime.of(11, 0),
        LocalTime.of(23, 0),
        LocalTime.of(11, 0),
        LocalTime.of(15, 0),
        LocalTime.of(18, 0),
        LocalTime.of(23, 0),
        new BigDecimal("2.00"),
        new BigDecimal("3.00"),
        new BigDecimal("10.00"),
        ADMIN_ID);
  }
}
