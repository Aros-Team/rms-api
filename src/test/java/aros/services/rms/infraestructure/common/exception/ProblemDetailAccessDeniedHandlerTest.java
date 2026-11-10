/* (C) 2026 */

package aros.services.rms.infraestructure.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Tests for {@link ProblemDetailAccessDeniedHandler}.
 *
 * <p><b>Feature:</b> Filter-level access denials (URL rules outside MVC) must return an RFC 7807
 * problem document instead of a body-less 403.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>Status is 403 and the media type is {@code application/problem+json;charset=UTF-8}
 *   <li>The JSON body exposes {@code type}, {@code title} "Forbidden", {@code status}, {@code
 *       detail} "Access denied" and {@code instance} (request URI)
 *   <li>No {@code WWW-Authenticate} header is emitted for a forbidden response
 *   <li>{@code Content-Length} is greater than zero (the body is never empty)
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Invokes the handler against a {@link MockHttpServletRequest} and {@link
 *       MockHttpServletResponse}
 *   <li>Parses the written JSON with the real Jackson 3 mapper and asserts each member
 * </ul>
 */
class ProblemDetailAccessDeniedHandlerTest {

  private final JsonMapper mapper = JsonMapper.builder().build();

  private final ProblemDetailAccessDeniedHandler handler =
      new ProblemDetailAccessDeniedHandler(mapper);

  @Test
  void should_write_problem_detail_body_with_forbidden_status() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/payroll");
    MockHttpServletResponse response = new MockHttpServletResponse();

    handler.handle(request, response, new AccessDeniedException("Access Denied"));

    assertEquals(403, response.getStatus());
    assertEquals("application/problem+json;charset=UTF-8", response.getContentType());

    JsonNode body = mapper.readTree(response.getContentAsString());
    assertEquals("about:blank", body.get("type").asString());
    assertEquals("Forbidden", body.get("title").asString());
    assertEquals(403, body.get("status").asInt());
    assertEquals("Access denied", body.get("detail").asString());
    assertEquals("/api/v1/payroll", body.get("instance").asString());

    assertTrue(response.getContentLength() > 0, "the 403 response must carry a non-empty body");
    assertTrue(
        response.getContentLength() == response.getContentAsByteArray().length,
        "Content-Length must match the serialized body size");
  }

  @Test
  void should_not_emit_www_authenticate_header_for_forbidden_response() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/payroll");
    MockHttpServletResponse response = new MockHttpServletResponse();

    handler.handle(request, response, new AccessDeniedException("Access Denied"));

    assertNull(response.getHeader("WWW-Authenticate"));
  }
}
