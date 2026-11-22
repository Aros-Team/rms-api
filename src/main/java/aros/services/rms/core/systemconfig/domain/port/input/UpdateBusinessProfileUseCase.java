/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain.port.input;

import aros.services.rms.core.systemconfig.domain.BusinessProfile;

/** Use case for updating the business profile. */
public interface UpdateBusinessProfileUseCase {

  /**
   * Validates and persists the business profile.
   *
   * @param profile the new profile
   * @return the persisted profile
   */
  BusinessProfile update(BusinessProfile profile);
}
