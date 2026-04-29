/* (C) 2026 */

package aros.services.rms.infraestructure.auth.jwt;

import aros.services.rms.core.area.domain.AreaId;
import aros.services.rms.core.user.domain.UserRole;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/** Implementation of JWT service. */
@Service
public class JwtServiceImpl implements JwtService {

  private static final Logger log = LoggerFactory.getLogger(JwtServiceImpl.class);
  private static final String JWT_NOT_CONFIGURED_ERROR =
      "JWT keys not configured. Cannot generate token. "
          + "Run './gradlew generate-jwt-keys' or 'task jwtkeys' to generate and add to .env file";

  private final JwtEncoder jwtEncoder;
  private final JwtDecoder jwtDecoder;
  private final String issuer;

  /**
   * Creates a new JWT service implementation.
   *
   * @param jwtEncoder the JWT encoder
   * @param jwtDecoder the JWT decoder
   * @param issuer the token issuer
   */
  public JwtServiceImpl(
      JwtEncoder jwtEncoder,
      JwtDecoder jwtDecoder,
      @Value("${app.jwt.issuer:rms-api}") String issuer) {
    this.jwtEncoder = jwtEncoder;
    this.jwtDecoder = jwtDecoder;
    this.issuer = issuer;
  }

  @Override
  public String generateAccessToken(String username, UserRole role, List<AreaId> areas) {
    if (jwtEncoder == null) {
      log.error(JWT_NOT_CONFIGURED_ERROR);
      throw new IllegalStateException("JWT not configured");
    }

    Instant now = Instant.now();

    List<Long> areaIds =
        areas != null ? areas.stream().map(AreaId::value).toList() : Collections.emptyList();

    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer(issuer)
            .subject(username)
            .issuedAt(now)
            .expiresAt(now.plusSeconds(3600))
            .claim("type", "access")
            .claim("role", role.name())
            .claim("areaIds", areaIds)
            .build();

    return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }

  @Override
  public String generateRefreshToken(String username) {
    if (jwtEncoder == null) {
      log.error(JWT_NOT_CONFIGURED_ERROR);
      throw new IllegalStateException("JWT not configured");
    }

    Instant now = Instant.now();

    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer(issuer)
            .subject(username)
            .issuedAt(now)
            .expiresAt(now.plus(Duration.ofDays(7)))
            .claim("type", "refresh")
            .build();

    return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }

  @Override
  public String generateTfaToken(String username) {
    if (jwtEncoder == null) {
      log.error(JWT_NOT_CONFIGURED_ERROR);
      throw new IllegalStateException("JWT not configured");
    }

    Instant now = Instant.now();

    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer(issuer)
            .subject(username)
            .issuedAt(now)
            .expiresAt(now.plus(Duration.ofMinutes(50)))
            .claim("type", "tfa")
            .build();

    return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }

  @Override
  public boolean validateToken(String token) {
    if (jwtDecoder == null) {
      return false;
    }
    try {
      jwtDecoder.decode(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public String getUsernameFromToken(String token) {
    if (jwtDecoder == null) {
      return null;
    }
    return jwtDecoder.decode(token).getSubject();
  }

  @Override
  public UserRole getRoleFromToken(String token) {
    if (jwtDecoder == null) {
      return null;
    }
    String role = jwtDecoder.decode(token).getClaimAsString("role");
    return role != null ? UserRole.valueOf(role) : null;
  }

  @Override
  public List<AreaId> getAreasFromToken(String token) {
    if (jwtDecoder == null) {
      return Collections.emptyList();
    }
    List<Long> areaIds = jwtDecoder.decode(token).getClaim("areaIds");
    if (areaIds == null) {
      return Collections.emptyList();
    }
    return areaIds.stream().map(AreaId::of).toList();
  }
}
