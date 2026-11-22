/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain.port.output;

import aros.services.rms.core.systemconfig.domain.BusinessProfile;
import java.util.Optional;

/** Output port for the business profile singleton. */
public interface BusinessProfilePort {

  /**
   * Returns the stored business profile.
   *
   * @return the profile when it has been persisted
   */
  Optional<BusinessProfile> find();

  /**
   * Persists the business profile.
   *
   * @param profile the profile to save
   * @return the saved profile
   */
  BusinessProfile save(BusinessProfile profile);
}
