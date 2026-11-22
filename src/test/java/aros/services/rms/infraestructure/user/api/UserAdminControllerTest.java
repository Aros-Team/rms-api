/* (C) 2026 */

package aros.services.rms.infraestructure.user.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.area.domain.AreaId;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserEmail;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.domain.UserRole;
import aros.services.rms.core.user.domain.UserStatus;
import aros.services.rms.core.user.port.input.GetAllUsersUseCase;
import aros.services.rms.core.user.port.input.UpdateUserAccessUseCase;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import aros.services.rms.infraestructure.common.exception.GlobalExceptionHandler;
import aros.services.rms.infraestructure.image.storage.local.LocalResourceConfig;
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests for {@link UserAdminController}.
 *
 * <p><b>Feature:</b> admin user and role management from settings.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>Lists users with role, status and area ids but never a password
 *   <li>Updates role, areas and status for an administrator
 *   <li>Rejects workers with 403 and anonymous callers with 401
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Loads the controller in a WebMvc slice with method security enabled
 *   <li>Mocks the use cases and resolves the acting admin by email
 * </ul>
 */
@WebMvcTest(
    value = UserAdminController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = LocalResourceConfig.class))
@Import({UserAdminControllerTest.TestSecurityConfig.class, GlobalExceptionHandler.class})
class UserAdminControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private GetAllUsersUseCase getAllUsersUseCase;
  @MockitoBean private UpdateUserAccessUseCase updateUserAccessUseCase;
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

  @Test
  void should_list_users_without_passwords() throws Exception {
    when(getAllUsersUseCase.getAll()).thenReturn(List.of(worker()));

    mockMvc
        .perform(get("/api/v1/users").with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].id").value(1001))
        .andExpect(jsonPath("$.data[0].role").value("WORKER"))
        .andExpect(jsonPath("$.data[0].status").value("ACTIVE"))
        .andExpect(jsonPath("$.data[0].areaIds[0]").value(2))
        .andExpect(jsonPath("$.data[0].password").doesNotExist());
  }

  @Test
  void should_change_role() throws Exception {
    User promoted = worker();
    promoted.changeRole(UserRole.ADMIN);
    when(userRepositoryPort.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser()));
    when(updateUserAccessUseCase.changeRole(eq(1001L), eq(UserRole.ADMIN), eq(1L)))
        .thenReturn(promoted);

    mockMvc
        .perform(
            patch("/api/v1/users/1001/role")
                .with(adminJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"ADMIN\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.role").value("ADMIN"));
  }

  @Test
  void should_change_areas() throws Exception {
    when(updateUserAccessUseCase.changeAreas(eq(1001L), any())).thenReturn(worker());

    mockMvc
        .perform(
            patch("/api/v1/users/1001/areas")
                .with(adminJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"areaIds\":[2]}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.areaIds[0]").value(2));
  }

  @Test
  void should_change_status() throws Exception {
    User inactive = worker();
    inactive.setStatus(UserStatus.INACTIVE);
    when(userRepositoryPort.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser()));
    when(updateUserAccessUseCase.changeStatus(eq(1001L), eq(UserStatus.INACTIVE), eq(1L)))
        .thenReturn(inactive);

    mockMvc
        .perform(
            patch("/api/v1/users/1001/status")
                .with(adminJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"INACTIVE\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("INACTIVE"));
  }

  @Test
  void should_reject_invalid_role_value() throws Exception {
    mockMvc
        .perform(
            patch("/api/v1/users/1001/role")
                .with(adminJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"SUPERADMIN\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
  }

  @Test
  void should_forbid_worker_from_listing_users() throws Exception {
    mockMvc
        .perform(get("/api/v1/users").with(workerJwt()))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
  }

  @Test
  void should_reject_anonymous_caller() throws Exception {
    mockMvc.perform(get("/api/v1/users")).andExpect(status().isUnauthorized());
  }

  private static User worker() {
    return new User(
        UserId.of(1001L),
        "1234567890",
        "Waiter",
        new UserEmail("waiter@rms.local"),
        "encoded",
        "Address",
        "3000000000",
        UserRole.WORKER,
        UserStatus.ACTIVE,
        List.of(AreaId.of(2L)));
  }

  private static User adminUser() {
    return new User(
        UserId.of(1L),
        "9999999999",
        "Admin",
        new UserEmail("admin@test.com"),
        "encoded",
        "Address",
        "3000000001",
        UserRole.ADMIN,
        UserStatus.ACTIVE,
        List.of(AreaId.of(1L)));
  }
}
