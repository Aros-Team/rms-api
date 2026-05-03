/* (C) 2026 */

package aros.services.rms.core.user.port.input;

/** Input port for user deletion operations. */
public interface DeleteUserUseCase {
  /**
   * Deletes a user by ID.
   *
   * @param userId the user ID to delete
   */
  void delete(Long userId);
}
