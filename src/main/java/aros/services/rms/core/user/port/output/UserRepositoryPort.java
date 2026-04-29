/* (C) 2026 */

package aros.services.rms.core.user.port.output;

import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.domain.UserStatus;
import aros.services.rms.core.user.domain.UserWithAreas;
import java.util.List;
import java.util.Optional;

/** Output port for user persistence operations. */
public interface UserRepositoryPort {
  /**
   * Finds a user by their email address.
   *
   * @param email the email to search
   * @return Optional containing the user if found
   */
  Optional<User> findByEmail(String email);

  /**
   * Finds a user with areas by their email address.
   *
   * @param email the email to search
   * @return Optional containing the user with areas if found
   */
  Optional<UserWithAreas> findByEmailWithAreas(String email);

  /**
   * Finds a user by their identifier.
   *
   * @param id the user identifier
   * @return Optional containing the user if found
   */
  Optional<User> findById(UserId id);

  /**
   * Checks if a user exists by email or document.
   *
   * @param document the document to check
   * @param email the email to check
   * @return true if a user exists with either
   */
  boolean existsByEmailOrDocument(String document, String email);

  /**
   * Retrieves all users.
   *
   * @return list of all users
   */
  List<User> findAll();

  /**
   * Finds all users with a specific status.
   *
   * @param status the user status to search
   * @return list of users with the specified status
   */
  List<User> findByStatus(UserStatus status);

  /**
   * Saves a user to the repository.
   *
   * @param user the user to save
   * @return the saved user
   */
  User save(User user);
}
