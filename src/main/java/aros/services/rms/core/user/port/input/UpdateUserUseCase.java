/* (C) 2026 */
package aros.services.rms.core.user.port.input;

import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.port.dto.UpdateUserInfo;

public interface UpdateUserUseCase {
  User update(Long userId, UpdateUserInfo info);
}
