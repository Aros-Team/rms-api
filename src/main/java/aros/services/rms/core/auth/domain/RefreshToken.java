/* (C) 2026 */

package aros.services.rms.core.auth.domain;

import aros.services.rms.core.user.domain.UserId;
import java.time.Instant;

/** Domain model representing a refresh token for session maintenance. */
public class RefreshToken {
  private RefreshTokenId id;
  private UserId userId;
  private String tokenHash;
  private Instant expiresAt;
  private boolean revoked;
  private Instant createdAt;

  /**
   * Creates a new RefreshToken instance.
   *
   * @param id the token identifier
   * @param userId the user identifier
   * @param tokenHash the hashed token value
   * @param expiresAt when the token expires
   * @param revoked whether the token has been revoked
   * @param createdAt when the token was created
   */
  public RefreshToken(
      RefreshTokenId id,
      UserId userId,
      String tokenHash,
      Instant expiresAt,
      boolean revoked,
      Instant createdAt) {
    this.id = id;
    this.userId = userId;
    this.tokenHash = tokenHash;
    this.expiresAt = expiresAt;
    this.revoked = revoked;
    this.createdAt = createdAt;
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
   * Checks if the token is valid (not revoked and not expired).
   *
   * @return true if the token is valid
   */
  public boolean isValid() {
    return !revoked && !isExpired();
  }

  /** Revokes this token. */
  public void revoke() {
    this.revoked = true;
  }

  /**
   * Gets the token identifier.
   *
   * @return the token id
   */
  public RefreshTokenId getId() {
    return id;
  }

  /**
   * Gets the user identifier.
   *
   * @return the user id
   */
  public UserId getUserId() {
    return userId;
  }

  /**
   * Gets the token hash.
   *
   * @return the token hash string
   */
  public String getTokenHash() {
    return tokenHash;
  }

  /**
   * Gets the expiration instant.
   *
   * @return when the token expires
   */
  public Instant getExpiresAt() {
    return expiresAt;
  }

  /**
   * Checks if the token is revoked.
   *
   * @return true if revoked
   */
  public boolean isRevoked() {
    return revoked;
  }

  /**
   * Gets the creation instant.
   *
   * @return when the token was created
   */
  public Instant getCreatedAt() {
    return createdAt;
  }
}
