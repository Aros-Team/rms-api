/* (C) 2026 */

package aros.services.rms.infraestructure.common.exception;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.infraestructure.user.config.AdminInitializer;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests for {@link GlobalExceptionHandler} and the HTTP status contract of routing and body-parsing
 * failures.
 *
 * <p><b>Feature:</b> Unknown routes return 404, a wrong HTTP method returns 405, and a malformed
 * JSON body returns 400, all as RFC 7807 problem documents rather than a 500.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>{@code GET /api/v1/no-such-endpoint} - 404 with {@code Endpoint not found}
 *   <li>{@code GET /api/v1/payroll/events} (POST-only path) - 405 with an {@code Allow} header
 *   <li>{@code POST /api/v1/orders} with broken JSON - 400
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Boots the real application context against H2 and mocks only the JWT decoder
 *   <li>Issues the requests through MockMvc with a valid bearer token so routing, not
 *       authentication, decides the status
 * </ul>
 */
@SpringBootTest(
    properties = {
      "app.jwt.public-key=unused",
      "app.jwt.private-key=unused",
      "spring.datasource.url=jdbc:h2:mem:errcontract;MODE=MySQL;DB_CLOSE_DELAY=-1",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.datasource.username=sa",
      "spring.datasource.password=sa",
      "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
      "spring.jpa.hibernate.ddl-auto=none",
      "spring.flyway.enabled=false",
      "spring.sql.init.mode=never"
    })
@AutoConfigureMockMvc
class ApiErrorStatusIntegrationTest {

  @MockitoBean private AdminInitializer adminInitializer;
  @MockitoBean private JwtDecoder jwtDecoder;

  @Autowired private MockMvc mockMvc;

  @DynamicPropertySource
  static void configureJwtKeys(DynamicPropertyRegistry registry) throws Exception {
    KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
    gen.initialize(2048);
    KeyPair kp = gen.generateKeyPair();
    String pubPem =
        "-----BEGIN PUBLIC KEY-----\n"
            + Base64.getEncoder().encodeToString(kp.getPublic().getEncoded())
            + "\n-----END PUBLIC KEY-----";
    String privPem =
        "-----BEGIN PRIVATE KEY-----\n"
            + Base64.getEncoder().encodeToString(kp.getPrivate().getEncoded())
            + "\n-----END PRIVATE KEY-----";
    registry.add("app.jwt.public-key", () -> pubPem);
    registry.add("app.jwt.private-key", () -> privPem);
  }

  @BeforeEach
  void setUp() {
    Jwt jwt =
        new Jwt(
            "token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            Map.of("alg", "RS256"),
            Map.of("sub", "admin@test.com", "role", "ADMIN"));
    when(jwtDecoder.decode(anyString())).thenReturn(jwt);
  }

  @Test
  void shouldReturn404_whenRouteDoesNotExist() throws Exception {
    mockMvc
        .perform(get("/api/v1/no-such-endpoint").header("Authorization", "Bearer test"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("not found")));
  }

  @Test
  void shouldReturn405_whenMethodIsNotSupported() throws Exception {
    mockMvc
        .perform(get("/api/v1/payroll/events").header("Authorization", "Bearer test"))
        .andExpect(status().isMethodNotAllowed())
        .andExpect(header().exists("Allow"))
        .andExpect(jsonPath("$.status").value(405));
  }

  @Test
  void shouldReturn400_whenJsonBodyIsMalformed() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/orders")
                .header("Authorization", "Bearer test")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ this is not json"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.detail").isNotEmpty());
  }
}
