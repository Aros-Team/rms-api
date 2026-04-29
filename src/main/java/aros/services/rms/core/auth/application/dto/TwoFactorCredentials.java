/* (C) 2026 */

package aros.services.rms.core.auth.application.dto;

import aros.services.rms.core.user.domain.UserEmail;

/** Record containing two-factor authentication credentials. */
public record TwoFactorCredentials(UserEmail username, String code, String deviceHash) {}
