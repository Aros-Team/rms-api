/* (C) 2026 */

package aros.services.rms.core.auth.application.dto;

import aros.services.rms.core.user.domain.UserEmail;

/** Record containing user credentials for authentication. */
public record Credentials(UserEmail username, String password, String deviceHash) {}
