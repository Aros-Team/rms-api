/* (C) 2026 */

package aros.services.rms.core.auth.domain;

/** Domain model representing authentication tokens (access and refresh). */
public class AuthToken {
  private String access;

  private String refresh;

  /**
   * Creates a new AuthToken with access and refresh tokens.
   *
   * @param access the access token string
   * @param refresh the refresh token string
   */
  public AuthToken(String access, String refresh) {
    this.access = access;
    this.refresh = refresh;
  }

  /**
   * Gets the access token.
   *
   * @return the access token string
   */
  public String getAccess() {
    return access;
  }

  /**
   * Gets the refresh token.
   *
   * @return the refresh token string
   */
  public String getRefresh() {
    return refresh;
  }
}
