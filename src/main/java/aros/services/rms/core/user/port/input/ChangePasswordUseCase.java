/* (C) 2026 */

package aros.services.rms.core.user.port.input;

/** Input port for password change operations. */
public interface ChangePasswordUseCase {
  /**
   * Changes user password.
   *
   * @param email user email
   * @param currentPassword current password for verification
   * @param newPassword new password to set
   */
  void changePassword(String email, String currentPassword, String newPassword);
}
