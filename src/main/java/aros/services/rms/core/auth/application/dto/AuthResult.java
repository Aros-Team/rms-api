/* (C) 2026 */
package aros.services.rms.core.auth.application.dto;

public sealed interface AuthResult permits AuthResult.Success, AuthResult.RequiresTFA {
  String username();

  AuthResultType type();

  record Success(String username, String acessToken, String refreshToken) implements AuthResult {
    @Override
    public AuthResultType type() {
      return AuthResultType.SUCCESS;
    }
  }

  record RequiresTFA(String username, String acessToken) implements AuthResult {
    @Override
    public AuthResultType type() {
      return AuthResultType.TFA_REQUIRED;
    }
  }
}
