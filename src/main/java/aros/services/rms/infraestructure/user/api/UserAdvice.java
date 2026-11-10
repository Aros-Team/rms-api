/* (C) 2026 */

package aros.services.rms.infraestructure.user.api;

import aros.services.rms.core.area.application.exception.AreaNotFoundException;
import aros.services.rms.core.user.application.exception.UserAlreadyExistsException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Exception advice for User operations. */
@RestControllerAdvice(assignableTypes = {WorkerController.class})
@Order(1)
public class UserAdvice {

  /**
   * Builds a problem response for the given status and detail.
   *
   * @param status the HTTP status
   * @param detail human-readable explanation
   * @return a {@code application/problem+json} response
   */
  private static ResponseEntity<ProblemDetail> problem(HttpStatus status, String detail) {
    ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail);
    body.setTitle(status.getReasonPhrase());
    return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(body);
  }

  /** Handles UserAlreadyExistsException. */
  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<ProblemDetail> handleAlreadyExistingUser(UserAlreadyExistsException e) {
    return problem(HttpStatus.CONFLICT, e.getMessage());
  }

  /** Handles AreaNotFoundException. */
  @ExceptionHandler(AreaNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleAreaNotFound(AreaNotFoundException e) {
    return problem(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
  }
}
