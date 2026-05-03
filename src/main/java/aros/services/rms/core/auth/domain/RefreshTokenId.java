/* (C) 2026 */

package aros.services.rms.core.auth.domain;

/** Value object representing the unique identifier for a refresh token. */
public record RefreshTokenId(Long value) {
  /** Validates and normalizes the refresh token ID value. */
  public RefreshTokenId {
    if (value == null || value <= 0) {
      throw new IllegalArgumentException("El ID no puede ser nulo o negativo");
    }
  }

  /**
   * Creates a RefreshTokenId from a Long value.
   *
   * @param value the long value
   * @return a new RefreshTokenId instance
   */
  public static RefreshTokenId of(Long value) {
    return new RefreshTokenId(value);
  }
}
