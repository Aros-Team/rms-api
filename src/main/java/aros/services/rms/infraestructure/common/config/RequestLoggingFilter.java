/* (C) 2026 */
package aros.services.rms.infraestructure.common.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
@Order(1)
public class RequestLoggingFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
  private static final String REQUEST_ID = "requestId";
  private static final String START_TIME = "startTime";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String requestId = generateRequestId();
    MDC.put(REQUEST_ID, requestId);

    long startTime = System.currentTimeMillis();
    MDC.put(START_TIME, String.valueOf(startTime));

    ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

    try {
      logRequest(request, requestId);
      filterChain.doFilter(request, responseWrapper);
    } catch (Exception e) {
      log.error(
          "Error procesando request: method={}, uri={}, error={}",
          request.getMethod(),
          request.getRequestURI(),
          e.getMessage(),
          e);
      throw e;
    } finally {
      long duration = System.currentTimeMillis() - startTime;
      logResponse(request, responseWrapper, duration, requestId);
      responseWrapper.copyBodyToResponse();
      MDC.remove(REQUEST_ID);
      MDC.remove(START_TIME);
    }
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.startsWith("/actuator") || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs");
  }

  private void logRequest(HttpServletRequest request, String requestId) {
    Map<String, String> headers = getHeaders(request);

    log.info(
        "REQUEST | id={} | method={} | uri={} | query={} | clientIp={} | headers={}",
        requestId,
        request.getMethod(),
        request.getRequestURI(),
        request.getQueryString(),
        getClientIp(request),
        sanitizeHeaders(headers));
  }

  private void logResponse(
      HttpServletRequest request, ContentCachingResponseWrapper response, long duration, String requestId) {

    String level = response.getStatus() >= 500 ? "ERROR" : "INFO";

    if (log.isInfoEnabled() || response.getStatus() >= 500) {
      log.info(
          "RESPONSE | id={} | method={} | uri={} | status={} | duration={}ms",
          requestId,
          request.getMethod(),
          request.getRequestURI(),
          response.getStatus(),
          duration);
    }
  }

  private Map<String, String> getHeaders(HttpServletRequest request) {
    Map<String, String> headers = new HashMap<>();
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String name = headerNames.nextElement();
      headers.put(name, request.getHeader(name));
    }
    return headers;
  }

  private Map<String, String> sanitizeHeaders(Map<String, String> headers) {
    Map<String, String> sanitized = new HashMap<>(headers);
    if (sanitized.containsKey("authorization")) {
      sanitized.put("authorization", "***");
    }
    if (sanitized.containsKey("cookie")) {
      sanitized.put("cookie", "***");
    }
    return sanitized;
  }

  private String getClientIp(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }

  private String generateRequestId() {
    return String.format(
        "%s-%d",
        Instant.now().toString().replace(":", "").replace(".", ""),
        (int) (Math.random() * 1000));
  }
}
