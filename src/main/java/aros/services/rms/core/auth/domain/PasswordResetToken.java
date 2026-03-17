/* (C) 2026 */
package aros.services.rms.core.auth.domain;

import aros.services.rms.core.user.domain.UserId;
import java.time.Instant;

public record PasswordResetToken(
    Long id, UserId userId, String tokenHash, Instant createdAt, Instant expiresAt, boolean used) {
  public PasswordResetToken {
    if (tokenHash == null || tokenHash.isBlank()) {
      throw new IllegalArgumentException("Token hash is required");
    }

    if (expiresAt == null) {
      throw new IllegalArgumentException("Expiration is required");
    }
  }

  public boolean isExpired() {
    return Instant.now().isAfter(expiresAt);
  }

  public PasswordResetToken markAsUsed() {
    return new PasswordResetToken(id, userId, tokenHash, createdAt, expiresAt, true);
  }
}
