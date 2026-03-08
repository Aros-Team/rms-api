/* (C) 2026 */
package aros.services.rms.core.auth.domain;

public class AuthToken {
  private String access;

  private String refresh;

  public AuthToken(String access, String refresh) {
    this.access = access;
    this.refresh = refresh;
  }

  public String getAccess() {
    return access;
  }

  public String getRefresh() {
    return refresh;
  }
}
