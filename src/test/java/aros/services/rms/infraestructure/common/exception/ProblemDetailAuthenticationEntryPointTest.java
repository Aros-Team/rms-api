/* (C) 2026 */

package aros.services.rms.infraestructure.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Tests for {@link ProblemDetailAuthenticationEntryPoint}.
 *
 * <p><b>Feature:</b> Unauthenticated requests must receive an RFC 7807 problem document instead of
 * a body-less 401, while keeping the standard {@code WWW-Authenticate} challenge.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>Status is 401 and the media type is {@code application/problem+json;charset=UTF-8}
 *   <li>The JSON body exposes {@code type}, {@code title}, {@code status}, {@code detail} and
 *       {@code instance} (request URI)
 *   <li>The {@code detail} never echoes token internals
 *   <li>The {@code WWW-Authenticate} header from the bearer entry point is preserved, including
 *       {@code error="invalid_token"} for invalid tokens
 *   <li>{@code Content-Length} is greater than zero (the body is never empty)
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Invokes the entry point against a {@link MockHttpServletRequest} and a {@link
 *       MockHttpServletResponse}
 *   <li>Parses the written JSON with the real Jackson 3 mapper and asserts each member
 * </ul>
 */
class ProblemDetailAuthenticationEntryPointTest {

  private final JsonMapper mapper = JsonMapper.builder().build();

  private final ProblemDetailAuthenticationEntryPoint entryPoint =
      new ProblemDetailAuthenticationEntryPoint(mapper);

  @Test
  void should_write_problem_detail_body_with_unauthorized_status() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth");
    MockHttpServletResponse response = new MockHttpServletResponse();

    entryPoint.commence(request, response, new InvalidBearerTokenException("Invalid access token"));

    assertEquals(401, response.getStatus());
    assertEquals("application/problem+json;charset=UTF-8", response.getContentType());

    JsonNode body = mapper.readTree(response.getContentAsString());
    assertEquals("about:blank", body.get("type").asString());
    assertEquals("Unauthorized", body.get("title").asString());
    assertEquals(401, body.get("status").asInt());
    assertEquals("Autenticación requerida", body.get("detail").asString());
    assertEquals("/api/auth", body.get("instance").asString());

    assertTrue(response.getContentLength() > 0, "the 401 response must carry a non-empty body");
    assertTrue(
        response.getContentAsByteArray().length > 0, "the 401 response body must not be empty");
  }

  @Test
  void should_preserve_www_authenticate_header_from_bearer_entry_point() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/orders");
    MockHttpServletResponse response = new MockHttpServletResponse();

    entryPoint.commence(request, response, new InvalidBearerTokenException("Invalid access token"));

    String challenge = response.getHeader("WWW-Authenticate");
    assertTrue(challenge != null && challenge.startsWith("Bearer"), "bearer challenge expected");
    assertTrue(
        challenge.contains("error=\"invalid_token\""),
        "invalid token error code must be preserved in the challenge");
  }

  @Test
  void should_not_leak_exception_message_in_body() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth");
    MockHttpServletResponse response = new MockHttpServletResponse();

    entryPoint.commence(
        request, response, new InvalidBearerTokenException("JWT signature mismatch at key kid-7"));

    String body = response.getContentAsString();
    assertTrue(
        !body.contains("JWT signature mismatch") && !body.contains("kid-7"),
        "token internals must not be exposed to the client");
  }
}
