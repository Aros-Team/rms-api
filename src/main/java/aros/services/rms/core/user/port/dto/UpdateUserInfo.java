/* (C) 2026 */

package aros.services.rms.core.user.port.dto;

import aros.services.rms.core.user.domain.UserEmail;

/** Data transfer object for updating an existing user. */
public record UpdateUserInfo(
    String document, String name, UserEmail email, String address, String phone) {}
