/* (C) 2026 */

package aros.services.rms.core.auth.application.service;

import aros.services.rms.core.auth.application.dto.UserFullInfo;
import aros.services.rms.core.auth.application.exception.UserNotFoundException;
import aros.services.rms.core.auth.port.input.GetCurrentAuthUserInfoUseCase;
import aros.services.rms.core.auth.port.output.CurrentUserPort;
import aros.services.rms.core.user.domain.UserEmail;
import aros.services.rms.core.user.domain.UserWithAreas;

/** Implementation for retrieving current authenticated user information. */
public class GetCurrentUserService implements GetCurrentAuthUserInfoUseCase {
  private final CurrentUserPort currentUserPort;

  /**
   * Creates the get current user service.
   *
   * @param currentUserPort port for retrieving current user info
   */
  public GetCurrentUserService(CurrentUserPort currentUserPort) {
    this.currentUserPort = currentUserPort;
  }

  @Override
  public UserFullInfo getInfo(UserEmail email) throws UserNotFoundException {
    UserWithAreas userWithAreas =
        this.currentUserPort
            .fetchCurrentUserInfo()
            .orElseThrow(() -> new UserNotFoundException("Not found"));

    UserFullInfo userFullInfo =
        new UserFullInfo(
            userWithAreas.id(),
            userWithAreas.document(),
            userWithAreas.name(),
            userWithAreas.email(),
            userWithAreas.password(),
            userWithAreas.address(),
            userWithAreas.phone(),
            userWithAreas.role(),
            userWithAreas.assignedAreas());

    return userFullInfo;
  }
}
