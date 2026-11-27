/* (C) 2026 */

package aros.services.rms.infraestructure.share.security;

import aros.services.rms.core.area.domain.AreaActivity;
import java.util.Collection;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Static helpers used by the method-security annotations in this package.
 *
 * <p>Keeping the claim handling in one place avoids leaking {@code null} claims into SpEL
 * expressions: a token issued before the {@code areaTypes} claim existed is treated as "no area"
 * instead of raising an evaluation error (which would surface as a 500 instead of a 403).
 *
 * <p>Activities are passed as {@link AreaActivity} constants so a rename in the enum fails at
 * compile time instead of silently denying access.
 */
public final class SecurityExpressions {

  private static final String ADMIN = "ADMIN";

  private static final String WORKER = "WORKER";

  private static final String AREA_TYPES_CLAIM = "areaTypes";

  private SecurityExpressions() {}

  /**
   * Returns whether the authenticated principal carries the ADMIN role.
   *
   * @param jwt the authenticated principal
   * @return true when the role claim equals ADMIN
   */
  public static boolean isAdmin(Jwt jwt) {
    return jwt != null && ADMIN.equals(jwt.getClaimAsString("role"));
  }

  /**
   * Returns whether the authenticated worker is assigned to the given activity.
   *
   * @param jwt the authenticated principal
   * @param activity the activity to look for
   * @return true when the worker's areaTypes claim contains the activity
   */
  public static boolean hasArea(Jwt jwt, AreaActivity activity) {
    return hasAnyArea(jwt, activity);
  }

  /**
   * Returns whether the authenticated worker is assigned to at least one of the given activities.
   *
   * @param jwt the authenticated principal
   * @param activities the activities to look for
   * @return true when any of the activities is present
   */
  public static boolean hasAnyArea(Jwt jwt, AreaActivity... activities) {
    if (jwt == null || !WORKER.equals(jwt.getClaimAsString("role"))) {
      return false;
    }
    Object claim = jwt.getClaims().get(AREA_TYPES_CLAIM);
    if (!(claim instanceof Collection<?> areaTypes)) {
      return false;
    }
    for (AreaActivity activity : activities) {
      if (areaTypes.contains(activity.name())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns whether the worker has at least one operational activity (PREPARATION, SERVE or CASH).
   *
   * @param jwt the authenticated principal
   * @return true when the activities claim contains at least one value
   */
  public static boolean hasAnyActivity(Jwt jwt) {
    if (jwt == null || !WORKER.equals(jwt.getClaimAsString("role"))) {
      return false;
    }
    Object claim = jwt.getClaims().get(AREA_TYPES_CLAIM);
    return claim instanceof Collection<?> activities && !activities.isEmpty();
  }
}
