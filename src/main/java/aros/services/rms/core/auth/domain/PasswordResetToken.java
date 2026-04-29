/* (C) 2026 */

package aros.services.rms.core.auth.domain;

import aros.services.rms.core.user.domain.UserId;
import java.time.Instant;

/** Record representing a password reset token with expiration validation. */
public record PasswordResetToken(
    Long id, UserId userId, String tokenHash, Instant createdAt, Instant expiresAt, boolean used) {
  /**
   * Canonical constructor with validation.
   *
   * @param id the token identifier
   * @param userId the user identifier
   * @param tokenHash the hashed token value
   * @param createdAt when the token was created
   * @param expiresAt when the token expires
   * @param used whether the token has been used
   * @throws IllegalArgumentException if tokenHash is null/blank or expiresAt is null
   */
  public PasswordResetToken {
    if (tokenHash == null || tokenHash.isBlank()) {
      throw new IllegalArgumentException("Token hash is required");
    }

    if (expiresAt == null) {
      throw new IllegalArgumentException("Expiration is required");
    }
  }

  /**
   * Checks if the token is expired.
   *
   * @return true if the token has expired
   */
  public boolean isExpired() {
    return Instant.now().isAfter(expiresAt);
  }

  /**
   * Marks this token as used.
   *
   * @return a new PasswordResetToken with used=true
   */
  public PasswordResetToken markAsUsed() {
    return new PasswordResetToken(id, userId, tokenHash, createdAt, expiresAt, true);
  }
}
