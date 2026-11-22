/* (C) 2026 */

package aros.services.rms.infraestructure.share.security;

import java.util.Collection;
import java.util.List;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Static helpers used by the method-security annotations in this package.
 *
 * <p>Keeping the claim handling in one place avoids leaking {@code null} claims into SpEL
 * expressions: a token issued before the {@code areaTypes} claim existed is treated as "no area"
 * instead of raising an evaluation error (which would surface as a 500 instead of a 403).
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
   * Returns whether the authenticated worker is assigned to the given area type.
   *
   * @param jwt the authenticated principal
   * @param areaType the area type to look for (e.g. SERVICE)
   * @return true when the worker's areaTypes claim contains the area type
   */
  public static boolean hasArea(Jwt jwt, String areaType) {
    if (jwt == null || !WORKER.equals(jwt.getClaimAsString("role"))) {
      return false;
    }
    Object claim = jwt.getClaims().get(AREA_TYPES_CLAIM);
    return claim instanceof Collection<?> areaTypes && areaTypes.contains(areaType);
  }

  /**
   * Returns whether the authenticated worker is assigned to at least one of two area types.
   *
   * @param jwt the authenticated principal
   * @param first first area type
   * @param second second area type
   * @return true when any of the area types is present
   */
  public static boolean hasAnyArea(Jwt jwt, String first, String second) {
    return hasAnyArea(jwt, List.of(first, second));
  }

  /**
   * Returns whether the authenticated worker is assigned to at least one of three area types.
   *
   * @param jwt the authenticated principal
   * @param first first area type
   * @param second second area type
   * @param third third area type
   * @return true when any of the area types is present
   */
  public static boolean hasAnyArea(Jwt jwt, String first, String second, String third) {
    return hasAnyArea(jwt, List.of(first, second, third));
  }

  /**
   * Returns whether the authenticated worker is assigned to at least one of five area types.
   *
   * @param jwt the authenticated principal
   * @param first first area type
   * @param second second area type
   * @param third third area type
   * @param fourth fourth area type
   * @param fifth fifth area type
   * @return true when any of the area types is present
   */
  public static boolean hasAnyArea(
      Jwt jwt, String first, String second, String third, String fourth, String fifth) {
    return hasAnyArea(jwt, List.of(first, second, third, fourth, fifth));
  }

  private static boolean hasAnyArea(Jwt jwt, Collection<String> candidates) {
    if (jwt == null || !WORKER.equals(jwt.getClaimAsString("role"))) {
      return false;
    }
    Object claim = jwt.getClaims().get(AREA_TYPES_CLAIM);
    if (!(claim instanceof Collection<?> areaTypes)) {
      return false;
    }
    for (String candidate : candidates) {
      if (areaTypes.contains(candidate)) {
        return true;
      }
    }
    return false;
  }
}
