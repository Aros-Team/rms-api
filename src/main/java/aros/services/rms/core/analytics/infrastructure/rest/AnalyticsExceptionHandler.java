/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.rest;

import aros.services.rms.core.analytics.domain.exception.AlertNotFoundException;
import aros.services.rms.core.analytics.domain.exception.AnalyticsRangeTooLargeException;
import aros.services.rms.core.analytics.domain.exception.InvalidAnalyticsPeriodException;
import aros.services.rms.core.analytics.domain.exception.InvalidAnalyticsRangeException;
import java.net.URI;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * RFC 7807 mapping for analytics-specific exceptions.
 *
 * <p>Kept in the analytics module (instead of {@code GlobalExceptionHandler}) so parallel lanes do
 * not edit the same shared file. The advice runs before the global handler so the
 * analytics-specific status codes and {@code type} URIs win.
 */
@RestControllerAdvice
@Order(5)
public class AnalyticsExceptionHandler {

  private static final URI INVALID_PERIOD = URI.create("/analytics/invalid-period");
  private static final URI INVALID_RANGE = URI.create("/analytics/invalid-range");
  private static final URI RANGE_TOO_LARGE = URI.create("/analytics/range-too-large");
  private static final URI ALERT_NOT_FOUND = URI.create("/analytics/alert-not-found");

  /** Maps a malformed period or bucket to 400. */
  @ExceptionHandler(InvalidAnalyticsPeriodException.class)
  public ResponseEntity<ProblemDetail> handleInvalidPeriod(InvalidAnalyticsPeriodException e) {
    return problem(HttpStatus.BAD_REQUEST, INVALID_PERIOD, e.getMessage());
  }

  /** Maps an inverted range to 422. */
  @ExceptionHandler(InvalidAnalyticsRangeException.class)
  public ResponseEntity<ProblemDetail> handleInvalidRange(InvalidAnalyticsRangeException e) {
    return problem(HttpStatus.UNPROCESSABLE_ENTITY, INVALID_RANGE, e.getMessage());
  }

  /** Maps an oversized range to 422. */
  @ExceptionHandler(AnalyticsRangeTooLargeException.class)
  public ResponseEntity<ProblemDetail> handleRangeTooLarge(AnalyticsRangeTooLargeException e) {
    return problem(HttpStatus.UNPROCESSABLE_ENTITY, RANGE_TOO_LARGE, e.getMessage());
  }

  /** Maps a missing alert to 404. */
  @ExceptionHandler(AlertNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleAlertNotFound(AlertNotFoundException e) {
    return problem(HttpStatus.NOT_FOUND, ALERT_NOT_FOUND, e.getMessage());
  }

  private static ResponseEntity<ProblemDetail> problem(HttpStatus status, URI type, String detail) {
    ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail);
    body.setTitle(status.getReasonPhrase());
    body.setType(type);
    return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(body);
  }
}
