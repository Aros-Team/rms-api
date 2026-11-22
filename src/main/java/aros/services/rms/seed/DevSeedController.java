/* (C) 2026 */

package aros.services.rms.seed;

import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Development-only reset surface used by Playwright and manual demos.
 *
 * <p>Enabled by {@code spring.profiles.active=development} and {@code app.seed.enabled=true}. Admin
 * role is required when JWT security is configured.
 */
@RestController
@RequestMapping("/api/v1/dev/seed")
@Profile("development")
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DevSeedController {

  private final SeedResetService service;

  /**
   * Creates the controller.
   *
   * @param service reset service
   */
  public DevSeedController(SeedResetService service) {
    this.service = service;
  }

  /**
   * Resets the dataset.
   *
   * @param request optional anchor and dataset
   * @return produced counts
   */
  @PostMapping("/reset")
  @PreAuthorize("principal.claims['role'] == 'ADMIN'")
  public SeedResetService.SeedResult reset(@RequestBody(required = false) ResetRequest request) {
    ResetRequest body = request == null ? new ResetRequest(null, "dev") : request;
    return service.reset(body.anchor(), body.dataset());
  }

  /**
   * Returns current seed row counts.
   *
   * @return counts per table
   */
  @GetMapping("/status")
  @PreAuthorize("principal.claims['role'] == 'ADMIN'")
  public Map<String, Object> status() {
    Map<String, Long> counts = service.status();
    Long orders = counts.getOrDefault("orders", 0L);
    int total = orders == null ? 0 : orders.intValue();
    return Map.of("status", "idle", "counts", counts, "orders", total);
  }

  /**
   * Maps a concurrent reset to HTTP 409.
   *
   * @param exception busy exception
   * @return problem-style body
   */
  @ExceptionHandler(SeedResetService.SeedBusyException.class)
  public ResponseEntity<Map<String, String>> busy(SeedResetService.SeedBusyException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(Map.of("error", "seed_busy", "detail", exception.getMessage()));
  }

  /** Reset request body. */
  public record ResetRequest(String anchor, String dataset) {}
}
