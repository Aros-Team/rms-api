/* (C) 2026 */

package aros.services.rms.core.auth.port.input;

import aros.services.rms.core.auth.application.dto.UserFullInfo;
import aros.services.rms.core.auth.application.exception.UserNotFoundException;
import aros.services.rms.core.user.domain.UserEmail;

/** Input port for retrieving current authenticated user's information. */
public interface GetCurrentAuthUserInfoUseCase {
  /**
   * Gets the current authenticated user's full information.
   *
   * @param email the user's email address
   * @return the user's full information including assigned areas
   * @throws UserNotFoundException if user does not exist
   */
  UserFullInfo getInfo(UserEmail email) throws UserNotFoundException;
}
