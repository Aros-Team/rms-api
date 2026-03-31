/* (C) 2026 */
package aros.services.rms.core.auth.domain;

import aros.services.rms.core.user.domain.UserId;
import java.time.Instant;

public class RefreshToken {
  private RefreshTokenId id;
  private UserId userId;
  private String tokenHash;
  private Instant expiresAt;
  private boolean revoked;
  private Instant createdAt;

  public RefreshToken(
      RefreshTokenId id, UserId userId, String tokenHash, Instant expiresAt, boolean revoked, Instant createdAt) {
    this.id = id;
    this.userId = userId;
    this.tokenHash = tokenHash;
    this.expiresAt = expiresAt;
    this.revoked = revoked;
    this.createdAt = createdAt;
  }

  public boolean isExpired() {
    return Instant.now().isAfter(expiresAt);
  }

  public boolean isValid() {
    return !revoked && !isExpired();
  }

  public void revoke() {
    this.revoked = true;
  }

  public RefreshTokenId getId() {
    return id;
  }

  public UserId getUserId() {
    return userId;
  }

  public String getTokenHash() {
    return tokenHash;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public boolean isRevoked() {
    return revoked;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
