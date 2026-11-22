/* (C) 2026 */

package aros.services.rms.seed;

/** Unchecked failure raised while generating or writing the seed dataset. */
final class SeedException extends RuntimeException {

  SeedException(String message) {
    super(message);
  }

  SeedException(String message, Throwable cause) {
    super(message, cause);
  }
}
