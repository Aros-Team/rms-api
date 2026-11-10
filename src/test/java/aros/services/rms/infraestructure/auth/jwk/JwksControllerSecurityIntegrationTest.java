/* (C) 2026 */

package aros.services.rms.infraestructure.auth.jwk;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.infraestructure.user.config.AdminInitializer;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Tests for {@link JwksController}.
 *
 * <p><b>Feature:</b> Auth security rules (public vs protected endpoints)
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>configure Jwt Keys
 *   <li>should Allow Public Access To Jwks Endpoint
 *   <li>should Reject Access To Protected Endpoint
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Loads full Spring context with embedded test database
 *   <li>Uses MockMvc to simulate HTTP requests and verify responses
 * </ul>
 */
@SpringBootTest(
    properties = {
      "app.jwt.public-key=unused",
      "app.jwt.private-key=unused",
      "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.datasource.username=sa",
      "spring.datasource.password=sa",
      "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
      "spring.jpa.hibernate.ddl-auto=none",
      "spring.flyway.enabled=false",
      "spring.sql.init.mode=never"
    })
@AutoConfigureMockMvc
class JwksControllerSecurityIntegrationTest {

  @MockitoBean private AdminInitializer adminInitializer;

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

  @Test
  void shouldAllowPublicAccessToJwksEndpoint() throws Exception {
    mockMvc
        .perform(get("/.well-known/jwks.json").accept("application/jwk-set+json"))
        .andExpect(status().isOk());
  }

  @Test
  void shouldRejectAccessToProtectedEndpoint() throws Exception {
    mockMvc.perform(get("/api/auth")).andExpect(status().isUnauthorized());
  }

  @Test
  void should_return_problem_detail_body_when_unauthenticated() throws Exception {
    MvcResult result =
        mockMvc
            .perform(get("/api/auth"))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Unauthorized"))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.detail").isNotEmpty())
            .andExpect(jsonPath("$.instance").value("/api/auth"))
            .andReturn();

    String body = result.getResponse().getContentAsString();
    assertTrue(!body.isEmpty(), "unauthenticated response must carry a body");
    assertNotEquals(0, result.getResponse().getContentLength(), "Content-Length must not be 0");
  }

  @Test
  void should_return_problem_detail_when_authenticated_user_lacks_admin_role() throws Exception {
    // The 403 here is produced by method security (@PreAuthorize) and rendered by
    // GlobalExceptionHandler, not by the URL-based AccessDeniedHandler. The filter-level
    // AccessDeniedHandler is covered directly by ProblemDetailAccessDeniedHandlerTest.
    MvcResult result =
        mockMvc
            .perform(
                get("/api/v1/analytics/config")
                    .with(
                        jwt()
                            .jwt(
                                token ->
                                    token
                                        .subject("worker@rms.local")
                                        .claim("role", "WORKER")
                                        .claim("type", "access"))))
            .andExpect(status().isForbidden())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Forbidden"))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.detail").isNotEmpty())
            .andExpect(jsonPath("$.instance").value("/api/v1/analytics/config"))
            .andReturn();

    String body = result.getResponse().getContentAsString();
    assertTrue(!body.isEmpty(), "forbidden response must carry a body");
    assertNotEquals(0, result.getResponse().getContentLength(), "Content-Length must not be 0");
  }
}
