/* (C) 2026 */

package aros.services.rms.core.user.port.dto;

import aros.services.rms.core.user.domain.UserEmail;

/** Data transfer object for creating a new user. */
public record CreateUserInfo(
    String document, String name, UserEmail email, String address, String phone) {}
