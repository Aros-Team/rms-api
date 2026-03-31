package aros.services.rms.core.auth.application.service;

import aros.services.rms.core.area.domain.Area;
import aros.services.rms.core.area.port.output.AreaRepositoryPort;
import aros.services.rms.core.auth.application.dto.UserFullInfo;
import aros.services.rms.core.auth.application.exception.UserNotFoundException;
import aros.services.rms.core.auth.port.input.GetCurrentAuthUserInfoUseCase;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserEmail;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import java.util.List;

public class GetCurrentUserService implements GetCurrentAuthUserInfoUseCase {
  private final UserRepositoryPort userPort;
  private final AreaRepositoryPort areaPort;

  public GetCurrentUserService(UserRepositoryPort userPort, AreaRepositoryPort areaPort) {
    this.userPort = userPort;
    this.areaPort = areaPort;
  }

  @Override
  public UserFullInfo getInfo(UserEmail email) throws UserNotFoundException {
    User user =
        userPort
            .findByEmail(email.value())
            .orElseThrow(() -> new UserNotFoundException("User not found"));

    List<Area> areas =
        areaPort.findByIdIn(user.getAssignedAreas().stream().map(a -> a.value()).toList());

    return new UserFullInfo(
        user.getId(),
        user.getDocument(),
        user.getName(),
        user.getEmail(),
        user.getPassword(),
        user.getAddress(),
        user.getPhone(),
        user.getRole(),
        areas);
  }
}
