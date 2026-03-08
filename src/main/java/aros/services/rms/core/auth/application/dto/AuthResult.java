/* (C) 2026 */
package aros.services.rms.core.auth.application.dto;

import aros.services.rms.core.auth.domain.AuthToken;

public record AuthResult(AuthResultType type, String username, AuthToken token) {}
