/* (C) 2026 */
package aros.services.rms.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
    @NotBlank(message = "app.version is required") String version,
    @NotBlank(message = "app.env is required") @Pattern(
            regexp = "^(dev|development|prod|production)$",
            message = "app.env must be 'dev' or 'prod'")
        String env,
    AdminProperties admin,
    JwtProperties jwt,
    EmailProperties email,
    CorsProperties cors) {
  public record AdminProperties(
      @NotBlank(message = "admin.email is required") @Pattern(
              regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$",
              message = "admin.email must be a valid email")
          String email,
      @NotBlank(message = "admin.dummy-email is required") @Pattern(
              regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$",
              message = "admin.dummy-email must be a valid email")
          String dummyEmail) {}

  public record JwtProperties(
      @NotBlank(message = "jwt.issuer is required") String issuer,
      @NotNull(message = "jwt.expiration-minutes is required") Duration expirationMinutes,
      @NotBlank(message = "jwt.public-key is required") String publicKey,
      @NotBlank(message = "jwt.private-key is required") String privateKey) {}

  public record EmailProperties(
      @NotBlank(message = "email.base-url is required") @Pattern(
              regexp = "^https?://.*",
              message = "email.base-url must start with http:// or https://")
          String baseUrl) {}

  public record CorsProperties(
      @NotBlank(message = "cors.allowed-origins is required") @Pattern(
              regexp = "^https?://[A-Za-z0-9.:/,_-]+(,https?://[A-Za-z0-9.:/,_-]+)*$",
              message = "cors.allowed-origins must be comma-separated valid URLs")
          String allowedOrigins) {}
}
