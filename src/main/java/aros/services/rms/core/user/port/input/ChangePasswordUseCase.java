/* (C) 2026 */
package aros.services.rms.core.user.port.input;

public interface ChangePasswordUseCase {
  void changePassword(String email, String currentPassword, String newPassword);
}
