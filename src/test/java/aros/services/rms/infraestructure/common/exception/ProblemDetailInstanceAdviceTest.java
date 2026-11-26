/* (C) 2026 */

package aros.services.rms.infraestructure.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests for {@link ProblemDetailInstanceAdvice}.
 *
 * <p><b>Feature:</b> Application-generated problem documents must expose the same {@code instance}
 * member as filter-level 401/403 documents, so clients see one uniform error shape.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>{@code supports} accepts {@code ProblemDetail} and {@code ResponseEntity<ProblemDetail>}
 *       handlers and rejects unrelated response types
 *   <li>{@code beforeBodyWrite} fills {@code instance} with the request URI when it is null
 *   <li>{@code beforeBodyWrite} fills the RFC 7807 default {@code type} ({@code about:blank}) when
 *       it is null, so application errors match filter-level errors
 *   <li>An already populated {@code instance} is preserved
 *   <li>A null body is returned untouched
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Builds {@link MethodParameter} descriptors from a tiny test controller
 *   <li>Calls the advice directly with mock servlet request/response wrappers and asserts the
 *       returned problem document
 * </ul>
 */
class ProblemDetailInstanceAdviceTest {

  private final ProblemDetailInstanceAdvice advice = new ProblemDetailInstanceAdvice();

  @Test
  void should_support_problem_detail_and_response_entity_problem_detail_returns() throws Exception {
    assertTrue(advice.supports(parameter("plainProblem", new Class<?>[0]), converterType()));
    assertTrue(
        advice.supports(parameter("problemResponseEntity", new Class<?>[0]), converterType()));
  }

  @Test
  void should_not_support_unrelated_response_types() throws Exception {
    assertFalse(
        advice.supports(parameter("stringResponseEntity", new Class<?>[0]), converterType()));
    assertFalse(advice.supports(parameter("stringBody", new Class<?>[0]), converterType()));
  }

  @Test
  void should_set_instance_to_request_uri_when_null() throws Exception {
    ProblemDetail body =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Pedido no encontrado");
    body.setTitle("Not Found");

    ProblemDetail result =
        advice.beforeBodyWrite(
            body,
            parameter("problemResponseEntity", new Class<?>[0]),
            MediaType.APPLICATION_PROBLEM_JSON,
            converterType(),
            new ServletServerHttpRequest(new MockHttpServletRequest("GET", "/api/v1/orders/999")),
            new ServletServerHttpResponse(new MockHttpServletResponse()));

    assertEquals(URI.create("/api/v1/orders/999"), result.getInstance());
    assertEquals(URI.create("about:blank"), result.getType());
    assertEquals("Pedido no encontrado", result.getDetail());
    assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatus());
  }

  @Test
  void should_preserve_existing_type() throws Exception {
    ProblemDetail body = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Conflict");
    body.setType(URI.create("https://rms.example/problems/conflict"));

    ProblemDetail result =
        advice.beforeBodyWrite(
            body,
            parameter("problemResponseEntity", new Class<?>[0]),
            MediaType.APPLICATION_PROBLEM_JSON,
            converterType(),
            new ServletServerHttpRequest(new MockHttpServletRequest("GET", "/api/v1/orders")),
            new ServletServerHttpResponse(new MockHttpServletResponse()));

    assertEquals(URI.create("https://rms.example/problems/conflict"), result.getType());
  }

  @Test
  void should_preserve_existing_instance() throws Exception {
    ProblemDetail body = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Acceso denegado");
    body.setInstance(URI.create("/already/set"));

    ProblemDetail result =
        advice.beforeBodyWrite(
            body,
            parameter("problemResponseEntity", new Class<?>[0]),
            MediaType.APPLICATION_PROBLEM_JSON,
            converterType(),
            new ServletServerHttpRequest(new MockHttpServletRequest("GET", "/api/auth")),
            new ServletServerHttpResponse(new MockHttpServletResponse()));

    assertEquals(URI.create("/already/set"), result.getInstance());
  }

  @Test
  void should_return_null_body_untouched() throws Exception {
    ProblemDetail result =
        advice.beforeBodyWrite(
            null,
            parameter("problemResponseEntity", new Class<?>[0]),
            MediaType.APPLICATION_PROBLEM_JSON,
            converterType(),
            new ServletServerHttpRequest(new MockHttpServletRequest("GET", "/api/auth")),
            new ServletServerHttpResponse(new MockHttpServletResponse()));

    assertNull(result);
  }

  private static MethodParameter parameter(String methodName, Class<?>[] parameterTypes)
      throws NoSuchMethodException {
    Method method = AdviceTestController.class.getMethod(methodName, parameterTypes);
    return new MethodParameter(method, -1);
  }

  @SuppressWarnings("unchecked")
  private static Class<? extends HttpMessageConverter<?>> converterType() {
    return (Class<? extends HttpMessageConverter<?>>) (Class<?>) HttpMessageConverter.class;
  }

  /** Minimal controller used only to obtain {@link MethodParameter} descriptors. */
  static class AdviceTestController {

    /** Returns a problem document directly. */
    public ProblemDetail plainProblem() {
      return null;
    }

    /** Returns a problem document wrapped in a response entity. */
    public ResponseEntity<ProblemDetail> problemResponseEntity() {
      return null;
    }

    /** Returns an unrelated body wrapped in a response entity. */
    public ResponseEntity<String> stringResponseEntity() {
      return null;
    }

    /** Returns an unrelated plain body. */
    public String stringBody() {
      return null;
    }
  }
}
