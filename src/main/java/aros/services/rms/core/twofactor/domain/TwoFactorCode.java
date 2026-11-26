/* (C) 2026 */

package aros.services.rms.core.twofactor.domain;

import aros.services.rms.core.user.domain.UserId;
import java.time.Instant;

/** Record representing a two-factor authentication code with expiration tracking. */
public record TwoFactorCode(
    TwoFactorCodeId id,
    UserId userId,
    String codeHash,
    Instant createdAt,
    Instant expiresAt,
    Instant usedAt) {
  /** Validates the two-factor code fields. */
  public TwoFactorCode {
    if (codeHash == null || codeHash.isBlank()) {
      throw new IllegalArgumentException("El hash del código es obligatorio");
    }

    if (expiresAt == null) {
      throw new IllegalArgumentException("La expiración es obligatoria");
    }
  }

  /**
   * Marks this code as used.
   *
   * @return a new TwoFactorCode with usedAt set to now
   */
  public TwoFactorCode markAsUsed() {
    return new TwoFactorCode(id, userId, codeHash, createdAt, expiresAt, Instant.now());
  }
}
