/* (C) 2026 */
package aros.services.rms.core.user.port.input;

import aros.services.rms.core.user.domain.User;
import java.util.List;

public interface GetAllUsersUseCase {
  List<User> getAll();
}
