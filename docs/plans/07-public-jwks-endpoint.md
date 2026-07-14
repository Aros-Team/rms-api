# Plan 07 — Public JWKS Endpoint

> Bounded context: `auth` (extend). Exposes the public half of the application's signing keys at `/.well-known/jwks.json` (RFC 7517 + RFC 8615) so clients can verify JWT signatures without hard-coding the key.

---

## 1. Why

Today the backend signs JWTs with an RSA key but only consumes them itself — there is no way for an external client (a different frontend deployment, a partner integration, a CLI tool, a test harness) to verify the signature without knowing the public key out-of-band. The public JWKS endpoint:

- Publishes the **public half** of every active signing key in a standard format.
- Lets clients cache the key set (rotated rarely).
- Supports **key rotation** via the standard `kid` (Key ID) JWT header — clients pick the right key by `kid` and the API can rotate without downtime.
- Standardizes on **RFC 7517** (JWK) and **RFC 8615** (Well-Known URIs).

### Out of scope

- OAuth2 authorization server (we are a resource server, not an authorization server).
- JWE / encrypted JWTs (current code only signs).
- HSM / KMS-backed key storage (single PEM is fine for v1).
- Mutual-TLS or DPoP-bound tokens.

---

## 2. Current state analysis

| Asset | Status | Gap |
|---|---|---|
| `RSAKey` bean | exists (`config/SecurityConfig.java:72`) | Built from PEM; **no `kid`**, no rotation support |
| `JwtEncoder` | exists | Wraps a single-key `ImmutableJWKSet` — cannot rotate without breaking tokens in flight |
| `JwtDecoder` | exists | Uses the same single public key |
| `JwtConfigValidator` | exists (`infraestructure/common/config/`) | Validates PEM presence; no concept of multiple keys |
| `spring-security-oauth2-resource-server` | on classpath | Spring Boot can **consume** a `jwk-set-uri` but provides no built-in **publisher** |
| Nimbus JOSE+JWT | transitive dep | `JWKSet.toPublicJWKSet()` ready to use |
| `/.well-known/jwks.json` | not exposed | This plan delivers it |

The `RSAKey` builder currently has no `keyID(...)` call — every JWT issued has no `kid` in its JOSE header, so even a single-key JWKS won't be usable by standards-compliant clients.

---

## 3. Database schema

No DB changes. Keys are loaded from environment variables (see § 6.1).

---

## 4. Domain layer (new types)

```
core/auth/domain/jwk/
├── JwkKey.java                    // record: kid, algorithm, use, publicJwk, hasPrivate
├── JwkKeyId.java                  // value object wrapping a non-blank String
├── JwkAlgorithm.java               // enum: RS256 (only — matches current code)
├── JwkUse.java                     // enum: SIGNATURE (sig) | ENCRYPTION (enc)
├── JwksDocument.java               // record: List<JwkKey>
├── exception/
│   ├── JwkKeyNotFoundException.java        → 404
│   ├── DuplicateJwkKeyIdException.java     → 409
│   ├── NoActiveSigningKeyException.java    → 503
│   └── InvalidJwkConfigurationException.java → 500
└── port/
    ├── input/
    │   └── PublishJwksUseCase.java
    └── output/
        └── JwkSourcePort.java              // supplies the active + retired key set
```

```java
public record JwkKey(
    JwkKeyId kid,
    JwkAlgorithm alg,           // RS256
    JwkUse use,                 // SIGNATURE
    com.nimbusds.jose.jwk.JWK publicJwk,    // Nimbus type — domain adapter maps it
    boolean hasPrivate
) {}

public record JwksDocument(List<JwkKey> keys) {
  public JwksDocument {
    if (keys == null || keys.isEmpty()) {
      throw new InvalidJwkConfigurationException("JWKS must contain at least one key");
    }
    keys = List.copyOf(keys);  // immutable
  }
  public Optional<JwkKey> findById(JwkKeyId kid) {
    return keys.stream().filter(k -> k.kid().equals(kid)).findFirst();
  }
}
```

> **Layer rule reminder**: `domain/` imports nothing from `org.springframework.*` or `jakarta.*`. The Nimbus `JWK` type lives in `com.nimbusds.jose.jwk.*`, which is a non-framework library — allowed.

---

## 5. Application layer

### 5.1 Use case

```
core/auth/application/service/PublishJwksService.java
```

```java
public class PublishJwksService implements PublishJwksUseCase {

  private final JwkSourcePort jwkSourcePort;

  public PublishJwksService(JwkSourcePort jwkSourcePort) {
    this.jwkSourcePort = jwkSourcePort;
  }

  @Override
  public JwksDocument publish() {
    JwksDocument doc = jwkSourcePort.loadAll();
    if (doc.keys().isEmpty()) {
      throw new NoActiveSigningKeyException();
    }
    return doc;
  }
}
```

### 5.2 Port

```java
public interface JwkSourcePort {
  /**
   * Returns every key the API is willing to publish: the active signing key
   * plus any retired keys still within the verification grace window.
   */
  JwksDocument loadAll();

  /** The single active signing key (must exist if loadAll is non-empty). */
  Optional<JwkKey> activeSigningKey();
}
```

---

## 6. Infrastructure layer

### 6.1 Configuration: keys from environment

### 6.1.1 Single-key mode (backwards compatible)

Keeps today's `.env` contract:

```env
JWT_PUBLIC_KEY="-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----"
JWT_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----...-----END PRIVATE KEY-----"
```

The `kid` is derived deterministically from the public key (see § 6.2). The plan does not break any existing deployment.

### 6.1.2 Multi-key rotation mode

When the project needs to rotate, the deployment sets:

```env
# Active signing key
JWT_ACTIVE_KID=k-2026-q3
JWT_PUBLIC_KEY_K_2026_Q3="-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----"
JWT_PRIVATE_KEY_K_2026_Q3="-----BEGIN PRIVATE KEY-----...-----END PRIVATE KEY-----"

# Retired key (still published for grace period)
JWT_PUBLIC_KEY_K_2026_Q2="-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----"
# No private key for retired keys — they sign nothing, only verify.
```

Both modes are read by `JwtConfigValidator` (extended, not replaced — see § 6.3).

### 6.2 `kid` derivation (deterministic, stable)

```java
public final class JwkKeyIdDeriver {
  private JwkKeyIdDeriver() {}

  /**
   * Derives a stable kid from an RSA public key by hashing the X.509 DER
   * encoding with SHA-256 and taking the first 16 hex chars.
   *
   * The same key always produces the same kid, so tokens issued before a
   * restart still validate against the published JWKS.
   */
  public static JwkKeyId from(RSAPublicKey publicKey) {
    String hex = HexFormat.of().formatHex(
        MessageDigest.getInstance("SHA-256")
                     .digest(publicKey.getEncoded()))   // X.509 SubjectPublicKeyInfo DER
                .substring(0, 16);
    return new JwkKeyId("k-" + hex);
  }
}
```

Why first 16 hex chars: 64 bits of entropy — collision-free for any realistic key population (10⁹ keys), short enough to be human-readable in logs.

### 6.3 Extended `JwtConfigValidator`

```java
@Component
public class JwtConfigValidator {

  public List<JwkKeyMaterial> allKeys() {
    if (multiKeyMode()) return loadMultiKeyMode();
    return List.of(singleKeyAsMaterial());      // backwards-compat
  }

  public JwkKeyId activeKeyId() {
    return multiKeyMode()
        ? new JwkKeyId(activeKid)
        : allKeys().get(0).kid();
  }
  // ... existing isConfigured() / getPublicKey() / getPrivateKey() kept for
  // callers that don't care about rotation (e.g. JwtServiceImpl today).
}
```

`JwkKeyMaterial` is an internal record in `infraestructure/auth/config/` carrying `(kid, RSAPublicKey, RSAPrivateKey-or-null)`. It is **not** a domain type — domain uses the framework-free `JwkKey` (which holds a Nimbus `JWK`).

### 6.4 `JwkSourceAdapter` (port implementation)

```
infraestructure/auth/jwk/JwkSourceAdapter.java
```

```java
@Component
public class JwkSourceAdapter implements JwkSourcePort {

  private final JwtConfigValidator validator;

  @Override
  public JwksDocument loadAll() {
    List<JwkKey> domainKeys = validator.allKeys().stream()
        .map(this::toDomainJwkKey)
        .toList();
    return new JwksDocument(domainKeys);
  }

  @Override
  public Optional<JwkKey> activeSigningKey() {
    JwkKeyId activeKid = validator.activeKeyId();
    return loadAll().findById(activeKid);
  }

  private JwkKey toDomainJwkKey(JwkKeyMaterial m) {
    try {
      RSAKey nimbus = m.privateKey() != null
          ? new RSAKey.Builder(m.publicKey()).privateKey(m.privateKey()).build()
          : new RSAKey.Builder(m.publicKey()).build();
      nimbus = new RSAKey.Builder(nimbus).keyID(m.kid().value()).build();
      return new JwkKey(m.kid(), JwkAlgorithm.RS256, JwkUse.SIGNATURE, nimbus,
                        m.privateKey() != null);
    } catch (JOSEException e) {
      throw new InvalidJwkConfigurationException("Failed to build JWK for kid=" + m.kid(), e);
    }
  }
}
```

### 6.5 New `JwksController`

```
infraestructure/auth/jwk/JwksController.java
```

```java
@RestController
@Tag(name = "Auth - JWKS")
public class JwksController {

  private final PublishJwksUseCase publishJwksUseCase;
  private final JwksCacheProperties cacheProps;

  public JwksController(PublishJwksUseCase publishJwksUseCase,
                        JwksCacheProperties cacheProps) {
    this.publishJwksUseCase = publishJwksUseCase;
    this.cacheProps = cacheProps;
  }

  @GetMapping(value = "/.well-known/jwks.json",
              produces = "application/jwk-set+json")     // RFC 7517 §3
  @Operation(
      summary = "Public JSON Web Key Set",
      description = "Returns the public half of every key the API can use to "
                  + "sign JWTs. Clients MUST verify tokens against the key "
                  + "whose 'kid' header matches.")
  @ApiResponse(responseCode = "200", description = "JWK Set")
  @ApiResponse(responseCode = "503", description = "No signing key configured")
  public ResponseEntity<JWKSet> getJwks() {
    JwksDocument doc = publishJwksUseCase.publish();
    JWKSet publicOnly = JWKSet.of(doc.keys().stream()
        .map(k -> k.publicJwk().toPublicJWK())          // strip private material
        .toList()).toJWKSet();

    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(cacheProps.getMaxAge())
                              .cachePublic()
                              .noTransform())
        .eTag(computeWeakEtag(doc))                     // § 6.6
        .header("X-Content-Type-Options", "nosniff")
        .body(publicOnly);
  }

  private String computeWeakEtag(JwksDocument doc) {
    String canonical = doc.keys().stream()
        .map(k -> k.kid().value() + ":" + k.publicJwk().toPublicJWK().toJSONObject().toString())
        .sorted()
        .collect(Collectors.joining("|"));
    return "W/\"" + Integer.toHexString(canonical.hashCode()) + "\"";
  }
}
```

> **Why `.toPublicJWK()`**: Nimbus's `JWK.toPublicJWK()` returns a copy with the `d`, `p`, `q`, `dp`, `dq`, `qi` private-key fields removed. Belt-and-suspenders: even if a future bug logged the raw JWKSet, the private material is not exposed.

### 6.6 Caching strategy

- `Cache-Control: public, max-age=3600, no-transform` — clients cache for 1 h.
- `ETag: W/"<weak-hash>"` — clients can revalidate cheaply; 304 Not Modified when unchanged.
- `X-Content-Type-Options: nosniff` — defense in depth.
- Content-Type `application/jwk-set+json` per RFC 7517 §3.

`JwksCacheProperties`:

```java
@ConfigurationProperties(prefix = "app.jwt.jwks")
@Validated
public record JwksCacheProperties(
    @NotNull @Positive Duration maxAge     // default 1h
) {}
```

### 6.7 Security filter chain — permit the endpoint

In `config/SecurityConfig.java`, add `/\\.well-known/jwks\\.json` to the `permitAll()` list in **both** the production and development branches. Spring Security's `requestMatchers` uses Ant patterns; the leading dot needs escaping.

```java
http.authorizeHttpRequests(auth -> auth
    .requestMatchers(
        "/.well-known/jwks.json",
        // ... existing permit-all list ...
    ).permitAll()
    .anyRequest().authenticated());
```

Also configure CORS for the endpoint: extend `CorsConfig` to include it in `allowedOrigins` with no credentials (JWKS is public).

### 6.8 Modify `JwtServiceImpl` to sign with `kid`

Today `JwtEncoder.encode(JwtEncoderParameters.from(claims))` produces a JWT with no `kid` header. After this plan:

```java
// JwtServiceImpl — updated
private final JwkSourceAdapter jwkSourceAdapter;        // NEW
private final JwkKeyId activeKid;                       // NEW (cached at boot)

// In each generate*Token() method:
JWK jwk = jwkSourceAdapter.activeSigningKey()
                            .orElseThrow(NoActiveSigningKeyException::new)
                            .publicJwk();

JwsHeader header = JwsHeader.with(MacAlgorithm.RS256 /* placeholder */) ...;
// Correct API for RS256:
JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).keyId(activeKid.value()).build();

return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
```

The encoder bean is rebuilt once at boot from `JwkSourceAdapter.loadAll()` — every key in the set is a valid signing candidate; the `kid` on the JWS header tells the receiver which one was used.

For the multi-key case, the encoder is constructed from a custom `JWKSource` that picks the key by `kid`:

```java
@Bean
public JwtEncoder jwtEncoder(JwkSourceAdapter adapter) {
  JWKSource<SecurityContext> source = (selector, ctx) -> {
    JWKSelector jwkSelector = (JWKSelector) selector;
    return jwkSelector.select(adapter.loadAll().keys().stream()
        .map(k -> k.publicJwk())
        .toList());
  };
  return new NimbusJwtEncoder(source);
}
```

This is the canonical pattern documented by Nimbus / Spring Security: the `JWKSource` resolves the key dynamically from a `kid` selector.

---

## 7. Properties (yml additions)

```yaml
app:
  jwt:
    issuer: rms-api
    expiration-minutes: 360
    public-key: ${JWT_PUBLIC_KEY:}
    private-key: ${JWT_PRIVATE_KEY:}
    jwks:
      cache:
        max-age: PT1H     # client-side cache; ETags still revalidate
```

---

## 8. Dependencies

**No new dependency.** Nimbus JOSE+JWT ships with `spring-boot-starter-oauth2-resource-server` (already a transitive in `build.gradle:73`):

```groovy
implementation 'org.springframework.boot:spring-boot-starter-oauth2-resource-server'
```

Verified against `build.gradle` — present.

If we ever want first-class support for kid-based caching, the only optional add would be `com.nimbusds:nimbus-jose-jwt:10.x` directly, but Spring Security 7 already pulls in a compatible version.

---

## 9. End-to-end flow

```
                                 JWKS endpoint
              Client                  │
                 │                    │
                 │   GET /.well-known/jwks.json
                 │ ──────────────────>│  permitAll, no auth
                 │                    │
                 │                    │ PublishJwksService.publish()
                 │                    │   → JwkSourcePort.loadAll()
                 │                    │     → JwtConfigValidator.allKeys()
                 │                    │       → env vars
                 │                    │     → JwkSourceAdapter.toDomainJwkKey(...)
                 │                    │
                 │   200 OK
                 │   application/jwk-set+json
                 │   Cache-Control: public, max-age=3600
                 │   ETag: W/"a3f7..."
                 │   {"keys": [
                 │     {"kty":"RSA","kid":"k-7a3b...","alg":"RS256",
                 │      "use":"sig","n":"...","e":"AQAB"}
                 │   ]}
                 │ <──────────────────│
                 │
                 ▼
        Verify signature with key matching JWT header "kid"
```

---

## 10. Concurrency model

- `JwkSourcePort.loadAll()` is **stateless** — reads env vars and parses PEM on every call. The PEM parsing is the expensive part (~10 ms per key); see § 11 for caching.
- The `JwtEncoder` bean is built once at boot. It holds the `JWKSource`; multiple sign requests serialize on the encoder (thread-safe per Nimbus docs).
- Hot reload of keys is **not** in v1 (rotations require a redeploy / restart). Documented; future enhancement.

---

## 11. Performance

- Cold call: parse PEM (~10 ms) + build `RSAKey` (~2 ms) + build `JWKSet` (~1 ms) per key.
- Add a `ConcurrentHashMap<JwkKeyId, RSAKey>` cache keyed by PEM content hash, so repeat `loadAll()` calls are O(1). Cache invalidates on `@RefreshScope` if added later.
- The HTTP response is small (< 2 KB for one RS256 key, ~ 4 KB for two) — cheap.

---

## 12. Tests

| Test class | Coverage |
|---|---|
| `JwkKeyIdDeriverTest` | Same key → same kid; different key → different kid |
| `JwkSourceAdapterTest` | Single-key mode returns one key; multi-key mode returns all; retired key has `hasPrivate=false` |
| `PublishJwksServiceTest` | Empty key set → 503; happy path → non-empty `JwksDocument` |
| `JwksControllerWebMvcTest` | `GET /.well-known/jwks.json` → 200, content-type `application/jwk-set+json`, body contains only public fields (no `d`, `p`, `q`, etc.), Cache-Control + ETag headers present |
| `JwksControllerSecurityIT` | Endpoint accessible **without** `Authorization` header; other endpoints still require it |
| `JwksControllerRotationIT` | Two keys configured → JWKS contains both; new tokens carry the active `kid`; old tokens still verify against the retired `kid` |
| `JwksResponseSchemaTest` | Body validates against RFC 7517 example shape (`kty`, `kid`, `alg`, `use`, `n`, `e` for RSA) |
| `JwksCachePropertiesBindingTest` | Invalid `max-age` (negative or zero) → startup fails |

---

## 13. Security considerations

| Concern | Mitigation |
|---|---|
| Private-key leakage via JWKS | `toPublicJWK()` strips all private fields before serializing |
| Key confusion attack (RSA used as HMAC key) | All published keys have `alg: "RS256"` and `use: "sig"` — clients can sanity-check |
| Stolen tokens after key compromise | Rotation: publish a new `kid`, retire the old; old tokens fail signature verification once the grace window closes |
| DoS via large JWKS | Hard cap at 5 keys per JWKSet; keys beyond cap are logged and rejected |
| Information leak via timing | Nimbus signing is constant-time per its JOSE 1.x spec |
| CSRF | Endpoint is GET only and stateless; CSRF disabled project-wide (existing) |

---

## 14. Edge cases

| Case | Handling |
|---|---|
| No keys configured | `/.well-known/jwks.json` returns 503 `NoActiveSigningKeyException` (RFC 7231) |
| `JWT_PUBLIC_KEY` set, `JWT_PRIVATE_KEY` empty | Single-key mode still publishes the key (verification-only) — JWKS still returns it |
| Same PEM re-imported | Same `kid` (deterministic), JWKS contains one entry |
| Two PEMs with identical modulus | `DuplicateJwkKeyIdException` → 500 on boot; rejected at config-load |
| `kid` collision across keys | Same as above |
| Key rotation in flight (old key removed) | Existing tokens using old `kid` fail verification. Mitigate with grace window (still publish retired key for N days) |
| Multi-key env var not fully populated (`PUBLIC_KEY_K_X` without `PRIVATE_KEY_K_X`) | Treat as retired key (verification-only); never use for signing |
| PEM contains `\r\n` line endings | `JwtConfigValidator.normalizeKey()` already strips them |

---

## 15. Acceptance criteria

- [ ] `JwtConfigValidator` extended to read single-key and multi-key env vars without breaking existing deployments
- [ ] Every `RSAKey` built in `SecurityConfig` carries a `kid` derived by `JwkKeyIdDeriver.from(publicKey)`
- [ ] `JwtServiceImpl` includes the `kid` JWS header on every generated token (access, refresh, TFA)
- [ ] `GET /.well-known/jwks.json` returns RFC 7517-compliant JSON, **public material only**
- [ ] Endpoint is in `permitAll()` of `SecurityConfig` (production branch) — reachable without auth
- [ ] `Content-Type: application/jwk-set+json`
- [ ] `Cache-Control: public, max-age=3600` + ETag set
- [ ] `X-Content-Type-Options: nosniff`
- [ ] CORS open for this path
- [ ] ProblemDetail (RFC 7807) returned on 503
- [ ] `./harness/harness.sh` exits 0

---

## 16. Rollout plan

| Step | Description | Risk |
|---|---|---|
| 1 | Add `JwkKeyIdDeriver` + extend `JwtConfigValidator` to derive `kid` automatically (single-key mode unchanged) | none |
| 2 | Wire `kid` into `JwtServiceImpl` JWS header | low — old tokens still verify (decoder uses modulus, ignores kid if missing) |
| 3 | Add `JwksController` + permit-all + CORS | low |
| 4 | Add multi-key env var support (`JWT_PUBLIC_KEY_K_<KID>` etc.) | low — additive |
| 5 | Add Nimbus `JWKSource` so encoder picks the active key by `kid` | medium — must keep single-key behavior identical |

Each step ships behind `feat/auth-jwks-*` branches; harness stays green at every step.

---

## 17. Open questions / follow-ups

- **Refresh without restart**: project-wide `@RefreshScope` is not enabled; if hot rotation is needed, add Spring Cloud Context and invalidate the `JwtEncoder` bean on signal.
- **Key deletion**: v1 does not delete retired keys. Add a `JWT_RETIRED_BEFORE` env var (ISO date) — keys with `kid` whose most-recent-rotation is older than this get filtered out of the JWKS.
- **HSM / KMS**: not needed today; if the deployment moves to GCP KMS later, swap `JwkSourcePort` impl to read from the KMS API. The port contract stays the same.
- **Cross-cluster JWKS replication**: not needed — each instance builds the same JWKS from the same env vars. If secrets diverge, JWKS diverges; document this in the deployment runbook.

---

## 18. References (researched via ctx7)

- **RFC 7517** — JSON Web Key (JWK). Format of `{"keys": [...]}`.
- **RFC 7515** — JSON Web Signature (JWS). `kid` header parameter for key selection.
- **RFC 8615** — Well-Known Uniform Resource Identifiers. `/.well-known/jwks.json` path.
- **RFC 7807** — Problem Details for HTTP APIs (consistent with other plans).
- **Spring Security 7 / Boot 4** — `NimbusJwtEncoder` accepts a `JWKSource<SecurityContext>` for kid-based key selection (confirmed via Spring Boot docs).
- **Nimbus JOSE+JWT 10.x** — `RSAKey.Builder`, `JWKSet.toPublicJWKSet()`, `JWK.toPublicJWK()` for safe serialization (confirmed via Nimbus changelog + source).
- **OWASP JWT cheat sheet** — recommendation to publish JWKS instead of hard-coding public keys in clients.