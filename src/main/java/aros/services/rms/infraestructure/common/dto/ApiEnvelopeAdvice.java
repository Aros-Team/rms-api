/* (C) 2026 */

package aros.services.rms.infraestructure.common.dto;

import jakarta.annotation.Nullable;
import java.io.InputStream;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * Wraps successful REST payloads in the standard {@link ApiResponse} envelope.
 *
 * <p>The advice only runs when {@code app.api.envelope.enabled=true} (the default; set it to {@code
 * false} to opt out). It never modifies error responses (RFC 7807 {@code ProblemDetail} or any
 * non-2xx status), empty responses (204), binary or streaming payloads, non-JSON content types, the
 * {@code application/jwk-set+json} media type, {@code /.well-known} paths, or bodies that are
 * already {@link ApiResponse} instances, so a payload can never be wrapped twice.
 */
@ControllerAdvice(basePackages = "aros.services.rms")
@ConditionalOnProperty(
    name = "app.api.envelope.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class ApiEnvelopeAdvice implements ResponseBodyAdvice<Object> {

  private static final String JWKS_MEDIA_TYPE = "application/jwk-set+json";
  private static final String WELL_KNOWN_PATH_PREFIX = "/.well-known";

  /** {@inheritDoc} */
  @Override
  public boolean supports(
      MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    Class<?> controller = returnType.getContainingClass();
    if (controller == null) {
      return false;
    }
    return controller.isAnnotationPresent(RestController.class)
        || controller.isAnnotationPresent(ResponseBody.class)
        || returnType.hasMethodAnnotation(ResponseBody.class);
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public Object beforeBodyWrite(
      @Nullable Object body,
      MethodParameter returnType,
      MediaType selectedContentType,
      Class<? extends HttpMessageConverter<?>> selectedConverterType,
      ServerHttpRequest request,
      ServerHttpResponse response) {
    if (body == null || body instanceof ApiResponse<?>) {
      return body;
    }
    if (body instanceof ProblemDetail) {
      return body;
    }
    if (!isWrappablePayload(body)) {
      return body;
    }
    if (isExcludedPath(request)) {
      return body;
    }
    if (!isJsonContentType(selectedContentType) || isExcludedContentType(selectedContentType)) {
      return body;
    }
    HttpStatusCode status = resolveStatus(response);
    if (!status.is2xxSuccessful() || status.value() == HttpStatus.NO_CONTENT.value()) {
      return body;
    }
    return ApiResponse.of(body);
  }

  private boolean isWrappablePayload(Object body) {
    return !(body instanceof byte[])
        && !(body instanceof Resource)
        && !(body instanceof InputStreamResource)
        && !(body instanceof StreamingResponseBody)
        && !(body instanceof InputStream);
  }

  private boolean isJsonContentType(@Nullable MediaType contentType) {
    if (contentType == null) {
      return false;
    }
    return MediaType.APPLICATION_JSON.isCompatibleWith(contentType)
        || contentType.getSubtype().toLowerCase().endsWith("+json");
  }

  /**
   * Media types that must be delivered verbatim: a JWKS document is signed material consumed by JWT
   * libraries, not an application payload, so wrapping it would break token verification.
   */
  private boolean isExcludedContentType(@Nullable MediaType contentType) {
    if (contentType == null) {
      return false;
    }
    String value = contentType.getType() + "/" + contentType.getSubtype();
    return JWKS_MEDIA_TYPE.equalsIgnoreCase(value);
  }

  /** Well-known discovery documents (JWKS, etc.) must never be wrapped. */
  private boolean isExcludedPath(ServerHttpRequest request) {
    String path = request.getURI().getPath();
    return path != null && path.startsWith(WELL_KNOWN_PATH_PREFIX);
  }

  private HttpStatusCode resolveStatus(ServerHttpResponse response) {
    if (response instanceof ServletServerHttpResponse servletResponse) {
      return HttpStatusCode.valueOf(servletResponse.getServletResponse().getStatus());
    }
    return HttpStatus.OK;
  }
}
