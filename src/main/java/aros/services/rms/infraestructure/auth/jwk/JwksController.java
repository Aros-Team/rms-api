/* (C) 2026 */

package aros.services.rms.infraestructure.auth.jwk;

import aros.services.rms.core.auth.domain.jwk.JwksDocument;
import aros.services.rms.core.auth.domain.jwk.exception.NoActiveSigningKeyException;
import aros.services.rms.core.auth.domain.jwk.port.input.PublishJwksUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Exposes the public JWKS document at the standard well-known URI. */
@RestController
@RequestMapping("/.well-known")
@Tag(name = "JWKS")
public class JwksController {

  private final PublishJwksUseCase publishJwksUseCase;
  private final JwksCacheProperties cacheProps;

  /** Creates a new JwksController. */
  public JwksController(PublishJwksUseCase publishJwksUseCase, JwksCacheProperties cacheProps) {
    this.publishJwksUseCase = publishJwksUseCase;
    this.cacheProps = cacheProps;
  }

  /**
   * Returns the public JWKS document (RFC 7517) containing all active signing keys. Private key
   * material is never exposed.
   *
   * @return 200 with JWKSet on success, 503 if no keys are configured
   */
  @GetMapping(value = "/jwks.json", produces = "application/jwk-set+json")
  @Operation(
      summary = "Public JWKS endpoint",
      description =
          "Returns the JSON Web Key Set (RFC 7517) containing the public half of all active"
              + " signing keys. Used by clients and identity providers to verify JWT signatures.")
  @ApiResponse(
      responseCode = "200",
      description = "JWKS document with public keys",
      content =
          @Content(
              mediaType = "application/jwk-set+json",
              schema = @Schema(implementation = String.class)))
  @ApiResponse(responseCode = "503", description = "No signing keys configured")
  public ResponseEntity<Map<String, Object>> getJwks() {
    try {
      JwksDocument doc = publishJwksUseCase.publish();
      com.nimbusds.jose.jwk.JWKSet publicOnly =
          new com.nimbusds.jose.jwk.JWKSet(
              doc.keys().stream().map(k -> k.publicJwk().toPublicJWK()).toList());

      return ResponseEntity.ok()
          .cacheControl(CacheControl.maxAge(cacheProps.maxAge()).cachePublic().noTransform())
          .eTag(computeWeakEtag(doc))
          .header("X-Content-Type-Options", "nosniff")
          .body(publicOnly.toJSONObject());
    } catch (NoActiveSigningKeyException e) {
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(null);
    }
  }

  private String computeWeakEtag(JwksDocument doc) {
    String canonical =
        doc.keys().stream()
            .map(k -> k.kid().value() + ":" + k.publicJwk().toPublicJWK().toJSONObject().toString())
            .sorted()
            .collect(Collectors.joining("|"));
    return "W/\"" + Integer.toHexString(canonical.hashCode()) + "\"";
  }
}
