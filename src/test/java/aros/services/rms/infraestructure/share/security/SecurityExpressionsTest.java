/* (C) 2026 */

package aros.services.rms.infraestructure.share.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import aros.services.rms.core.area.domain.AreaActivity;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Tests for {@link SecurityExpressions}.
 *
 * <p><b>Feature:</b> activity-based authorization for workers.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>a worker sees only the activities carried by the {@code areaTypes} claim
 *   <li>an absent claim denies every activity instead of failing
 *   <li>admins are handled by the role clause of the annotations, not as an activity
 * </ul>
 */
class SecurityExpressionsTest {

  private static Jwt worker(AreaActivity... activities) {
    return Jwt.withTokenValue("token")
        .header("alg", "none")
        .subject("worker@rms.test")
        .claim("type", "access")
        .claim("role", "WORKER")
        .claim("areaTypes", Arrays.stream(activities).map(Enum::name).toList())
        .build();
  }

  @Test
  void grantsOnlyTheActivitiesInTheClaim() {
    Jwt jwt = worker(AreaActivity.SERVE);

    assertTrue(SecurityExpressions.hasArea(jwt, AreaActivity.SERVE));
    assertFalse(SecurityExpressions.hasArea(jwt, AreaActivity.PREPARATION));
    assertFalse(SecurityExpressions.hasArea(jwt, AreaActivity.CASH));
  }

  @Test
  void grantsAnyOfTheRequestedActivities() {
    Jwt jwt = worker(AreaActivity.SERVE, AreaActivity.PREPARATION);

    assertTrue(SecurityExpressions.hasAnyArea(jwt, AreaActivity.SERVE, AreaActivity.CASH));
    assertTrue(SecurityExpressions.hasAnyArea(jwt, AreaActivity.PREPARATION, AreaActivity.CASH));
    assertFalse(SecurityExpressions.hasAnyArea(jwt, AreaActivity.CASH));
  }

  @Test
  void deniesEverythingWhenTheClaimIsMissing() {
    Jwt jwt =
        Jwt.withTokenValue("token")
            .header("alg", "none")
            .subject("worker@rms.test")
            .claim("type", "access")
            .claim("role", "WORKER")
            .build();

    assertFalse(SecurityExpressions.hasArea(jwt, AreaActivity.SERVE));
    assertFalse(SecurityExpressions.hasAnyArea(jwt, AreaActivity.SERVE, AreaActivity.CASH));
    assertFalse(SecurityExpressions.hasAnyActivity(jwt));
  }

  @Test
  void ignoresNonWorkerPrincipals() {
    Jwt admin =
        Jwt.withTokenValue("token")
            .header("alg", "none")
            .subject("admin@rms.test")
            .claim("type", "access")
            .claim("role", "ADMIN")
            .claim("areaTypes", List.of(AreaActivity.SERVE.name()))
            .build();

    assertFalse(SecurityExpressions.hasArea(admin, AreaActivity.SERVE));
    assertTrue(SecurityExpressions.isAdmin(admin));
  }

  @Test
  void reportsWhetherAnyOperationalActivityIsPresent() {
    Jwt worker = worker(AreaActivity.CASH);
    Jwt noActivities = worker();

    assertTrue(SecurityExpressions.hasAnyActivity(worker));
    assertFalse(SecurityExpressions.hasAnyActivity(noActivities));
  }
}
