/* (C) 2026 */

package aros.services.rms.core.auth.application.dto;

/** Record containing the final authentication result with tokens. */
public record AuthFinalResult(String username, String acessToken, String refreshToken) {
  //
}
