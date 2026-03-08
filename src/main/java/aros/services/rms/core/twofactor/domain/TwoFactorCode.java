/* (C) 2026 */
package aros.services.rms.core.twofactor.domain;

import aros.services.rms.core.user.domain.UserId;
import java.time.Instant;

public record TwoFactorCode(
    TwoFactorCodeId id,
    UserId userId,
    String codeHash,
    Instant createdAt,
    Instant expiresAt,
    Instant usedAt) {
  public TwoFactorCode {
    if (codeHash == null || codeHash.isBlank()) {
      throw new IllegalArgumentException("Code hash is required");
    }

    if (expiresAt == null) {
      throw new IllegalArgumentException("Expiration is required");
    }
  }

  public TwoFactorCode markAsUsed() {
    return new TwoFactorCode(id, userId, codeHash, createdAt, expiresAt, Instant.now());
  }
}
