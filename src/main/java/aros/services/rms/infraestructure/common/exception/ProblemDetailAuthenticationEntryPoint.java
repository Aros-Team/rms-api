/* (C) 2026 */

package aros.services.rms.infraestructure.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Authentication entry point that answers unauthenticated requests with an RFC 7807 {@code
 * application/problem+json} body instead of the body-less response produced by the default resource
 * server entry point.
 *
 * <p>The standard {@link BearerTokenAuthenticationEntryPoint} runs first, so the {@code
 * WWW-Authenticate} header (including {@code error="invalid_token"} / {@code error_description}) is
 * preserved. Afterwards this class writes the shared problem document with {@code status} 401,
 * {@code title} "Unauthorized", {@code detail} "Authentication required" and {@code instance} set
 * to the request URI. Token internals are never echoed to the client.
 */
@Component
public class ProblemDetailAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private static final String TITLE = "Unauthorized";

  private static final String DETAIL = "Authentication required";

  private final BearerTokenAuthenticationEntryPoint delegate =
      new BearerTokenAuthenticationEntryPoint();

  private final JsonMapper mapper;

  /**
   * Creates the entry point.
   *
   * @param mapper Jackson mapper used to serialize the problem document
   */
  public ProblemDetailAuthenticationEntryPoint(JsonMapper mapper) {
    this.mapper = mapper;
  }

  /** {@inheritDoc} */
  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {
    delegate.commence(request, response, authException);
    ProblemDetailWriter.write(request, response, mapper, HttpStatus.UNAUTHORIZED, TITLE, DETAIL);
  }
}
