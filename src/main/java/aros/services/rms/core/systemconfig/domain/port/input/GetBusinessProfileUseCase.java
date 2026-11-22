/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain.port.input;

import aros.services.rms.core.systemconfig.domain.BusinessProfile;

/** Use case for reading the business profile. */
public interface GetBusinessProfileUseCase {

  /**
   * Returns the stored business profile or documented defaults.
   *
   * @return the business profile
   */
  BusinessProfile get();
}
