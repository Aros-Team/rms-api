package aros.services.rms.infraestructure.user.api;

import aros.services.rms.core.area.application.exception.AreaNotFoundException;
import aros.services.rms.core.user.application.exception.UserAlreadyExistsException;
import aros.services.rms.infraestructure.common.exception.ErrorResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Exception advice for User operations. */
@RestControllerAdvice(assignableTypes = {UserController.class})
@Order(1)
public class UserAdvice {
  /** Handles UserAlreadyExistsException. */
  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleAlreadyExistingUser(UserAlreadyExistsException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ErrorResponse(HttpStatus.CONFLICT.value(), e.getMessage()));
  }

  /** Handles AreaNotFoundException. */
  @ExceptionHandler(AreaNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleAreaNotFound(AreaNotFoundException e) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(new ErrorResponse(HttpStatus.UNPROCESSABLE_CONTENT.value(), e.getMessage()));
  }
}
