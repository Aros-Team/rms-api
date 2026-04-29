package aros.services.rms.infraestructure.auth.api;

import aros.services.rms.core.auth.application.exception.InvalidRefreshTokenException;
import aros.services.rms.core.auth.application.exception.UserNotFoundException;
import aros.services.rms.infraestructure.common.exception.ErrorResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Global exception handler for authentication-related exceptions. */
@RestControllerAdvice
@Order(1)
public class AuthAdvice {
  /**
   * Handles user not found exceptions.
   *
   * @param e the exception
   * @return error response
   */
  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
  }

  /**
   * Handles invalid refresh token exceptions.
   *
   * @param e the exception
   * @return error response
   */
  @ExceptionHandler(InvalidRefreshTokenException.class)
  public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidRefreshTokenException e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), e.getMessage()));
  }
}
