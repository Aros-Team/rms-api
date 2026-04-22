/* (C) 2026 */
package aros.services.rms.core.auth.domain;

import aros.services.rms.core.user.domain.UserId;
import java.time.Instant;

public record AccountSetupToken(
    Long id, UserId userId, String tokenHash, Instant createdAt, Instant expiresAt, boolean used) {
  public AccountSetupToken {
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

  public AccountSetupToken markAsUsed() {
    return new AccountSetupToken(id, userId, tokenHash, createdAt, expiresAt, true);
  }
}
