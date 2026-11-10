/* (C) 2026 */

package aros.services.rms.infraestructure.common.exception;

import java.net.URI;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Normalizes the two RFC 7807 defaults that {@link ProblemDetail} leaves unset, so
 * application-generated problem documents are indistinguishable in shape from filter-level ones.
 *
 * <p>Application errors ({@code GlobalExceptionHandler}) set neither {@code instance} nor {@code
 * type}, while filter-level errors ({@link ProblemDetailAuthenticationEntryPoint}, {@link
 * ProblemDetailAccessDeniedHandler}) always do. This advice fills {@code instance} with the request
 * URI and {@code type} with the RFC 7807 default {@code about:blank} when they are {@code null}, so
 * every error exposes the exact same field set: {@code type}, {@code title}, {@code status}, {@code
 * detail} and {@code instance}.
 *
 * <p>This advice only touches {@link ProblemDetail} bodies. It never wraps, replaces or otherwise
 * alters any other payload, and it coexists with {@code ApiEnvelopeAdvice}, which already skips
 * problem documents.
 */
@ControllerAdvice
public class ProblemDetailInstanceAdvice implements ResponseBodyAdvice<ProblemDetail> {

  private static final URI DEFAULT_TYPE = URI.create("about:blank");

  /** {@inheritDoc} */
  @Override
  public boolean supports(
      MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    Class<?> parameterType = returnType.getParameterType();
    if (ProblemDetail.class.isAssignableFrom(parameterType)) {
      return true;
    }
    if (!ResponseEntity.class.isAssignableFrom(parameterType)) {
      return false;
    }
    Class<?> bodyType = ResolvableType.forMethodParameter(returnType).getGeneric(0).resolve();
    return bodyType != null && ProblemDetail.class.isAssignableFrom(bodyType);
  }

  /** {@inheritDoc} */
  @Override
  public ProblemDetail beforeBodyWrite(
      ProblemDetail body,
      MethodParameter returnType,
      MediaType selectedContentType,
      Class<? extends HttpMessageConverter<?>> selectedConverterType,
      ServerHttpRequest request,
      ServerHttpResponse response) {
    if (body == null) {
      return body;
    }
    if (body.getType() == null) {
      body.setType(DEFAULT_TYPE);
    }
    if (body.getInstance() != null) {
      return body;
    }
    if (request instanceof ServletServerHttpRequest servletRequest) {
      String requestUri = servletRequest.getServletRequest().getRequestURI();
      if (StringUtils.hasText(requestUri)) {
        body.setInstance(URI.create(requestUri));
      }
    }
    return body;
  }
}
