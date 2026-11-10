/* (C) 2026 */

package aros.services.rms.infraestructure.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.json.JsonMapper;

/**
 * Writes RFC 7807 {@code application/problem+json} bodies for responses produced outside the MVC
 * exception-handling pipeline, such as Spring Security filter failures.
 *
 * <p>This utility is the single definition of the problem field set shared by filter-level errors
 * and application errors: {@code type}, {@code title}, {@code status}, {@code detail} and {@code
 * instance}. It always writes a body and sets a non-zero {@code Content-Length}, so clients never
 * receive a body-less {@code 401}/{@code 403}.
 */
final class ProblemDetailWriter {

  /** Media type used for every problem response, including its UTF-8 charset. */
  static final String CONTENT_TYPE = "application/problem+json;charset=UTF-8";

  private static final String PROBLEM_TYPE = "about:blank";

  private ProblemDetailWriter() {}

  /**
   * Serializes and writes a problem document, guaranteeing the response has a non-empty body.
   *
   * @param request current request; its URI becomes {@code instance}
   * @param response response to write the problem document into
   * @param mapper Jackson mapper used for serialization
   * @param status HTTP status of the problem
   * @param title short reason phrase for the status
   * @param detail human-readable explanation without token or principal internals
   * @throws IOException if writing the response body fails
   */
  static void write(
      HttpServletRequest request,
      HttpServletResponse response,
      JsonMapper mapper,
      HttpStatus status,
      String title,
      String detail)
      throws IOException {
    if (response.isCommitted()) {
      return;
    }
    byte[] payload = mapper.writeValueAsBytes(body(request, status, title, detail));
    response.setStatus(status.value());
    response.setContentType(CONTENT_TYPE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentLength(payload.length);
    response.getOutputStream().write(payload);
    response.flushBuffer();
  }

  private static Map<String, Object> body(
      HttpServletRequest request, HttpStatus status, String title, String detail) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("type", PROBLEM_TYPE);
    body.put("title", title);
    body.put("status", status.value());
    body.put("detail", detail);
    body.put("instance", request.getRequestURI());
    return body;
  }
}
