/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain;

import lombok.Builder;
import lombok.Data;

/** Business identity and contact data shown on documents and used by the application. */
@Data
@Builder
public class BusinessProfile {
  private String restaurantName;
  private String legalName;
  private String taxId;
  private String address;
  private String phone;
  private String email;
  private String timezone;
  private String locale;

  /**
   * Returns the documented defaults used before the first save.
   *
   * @return a profile with empty contact fields and the bootstrap timezone and locale
   */
  public static BusinessProfile defaults() {
    return BusinessProfile.builder()
        .restaurantName("RMS")
        .legalName("")
        .taxId("")
        .address("")
        .phone("")
        .email("")
        .timezone("America/Bogota")
        .locale("es-CO")
        .build();
  }
}
