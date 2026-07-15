/* (C) 2026 */

package aros.services.rms.core.auth.domain.jwk;

import com.nimbusds.jose.jwk.JWK;

/** Represents a single JWK key with metadata. */
public record JwkKey(
    JwkKeyId kid, JwkAlgorithm alg, JwkUse use, JWK publicJwk, boolean hasPrivate) {}
