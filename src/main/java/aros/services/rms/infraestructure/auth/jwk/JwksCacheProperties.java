/* (C) 2026 */

package aros.services.rms.infraestructure.auth.jwk;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Configuration properties for JWKS cache. */
@ConfigurationProperties(prefix = "app.jwt.jwks")
@Validated
public record JwksCacheProperties(@NotNull Duration maxAge) {

  /**
   * Validates that the maxAge duration is positive (non-zero, non-negative).
   *
   * @return true if maxAge is positive
   */
  @AssertTrue
  public boolean isPositive() {
    return !maxAge.isNegative() && !maxAge.isZero();
  }
}
