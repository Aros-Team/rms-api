/* (C) 2026 */

package aros.services.rms.infraestructure.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Access denied handler that answers forbidden requests with an RFC 7807 {@code
 * application/problem+json} body instead of the default body-less {@code 403}.
 *
 * <p>The body uses the same field set as application errors: {@code status} 403, {@code title}
 * "Forbidden", {@code detail} "Access denied" and {@code instance} set to the request URI. No
 * principal or rule internals are exposed.
 */
@Component
public class ProblemDetailAccessDeniedHandler implements AccessDeniedHandler {

  private static final String TITLE = "Forbidden";

  private static final String DETAIL = "Acceso denegado";

  private final JsonMapper mapper;

  /**
   * Creates the access denied handler.
   *
   * @param mapper Jackson mapper used to serialize the problem document
   */
  public ProblemDetailAccessDeniedHandler(JsonMapper mapper) {
    this.mapper = mapper;
  }

  /** {@inheritDoc} */
  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException {
    ProblemDetailWriter.write(request, response, mapper, HttpStatus.FORBIDDEN, TITLE, DETAIL);
  }
}
