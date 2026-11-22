/* (C) 2026 */

package aros.services.rms.infraestructure.common.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.analytics.domain.exception.InvalidAnalyticsRangeException;
import aros.services.rms.core.analytics.infrastructure.rest.AnalyticsExceptionHandler;
import aros.services.rms.core.auth.application.exception.InvalidCredentialsException;
import aros.services.rms.core.order.application.exception.OrderNotFoundException;
import aros.services.rms.core.order.application.exception.TableNotAvailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tests for {@link GlobalExceptionHandler} and the RFC 7807 error contract across representative
 * status codes.
 *
 * <p><b>Feature:</b> Every application error exposes {@code type}, {@code title}, {@code status},
 * {@code detail} and {@code instance}, and never returns a bare or enveloped body.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>400 for invalid input
 *   <li>401 for invalid credentials
 *   <li>403 for a denied request
 *   <li>404 for a missing resource
 *   <li>409 for a conflicting state
 *   <li>422 for an invalid analytics range
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Builds a standalone MockMvc around a throwing test controller
 *   <li>Registers the same advices used at runtime, including the instance/type normalizer
 *   <li>Asserts status, media type and the five RFC 7807 members on every response
 * </ul>
 */
class ErrorContractTest {

  private static final String PROBLEM_JSON = MediaType.APPLICATION_PROBLEM_JSON_VALUE;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new ThrowingController())
            .setControllerAdvice(
                new GlobalExceptionHandler(),
                new AnalyticsExceptionHandler(),
                new ProblemDetailInstanceAdvice())
            .build();
  }

  @Test
  void shouldReturn400ProblemDetail_whenInputIsInvalid() throws Exception {
    assertProblem("/bad-request", 400, "Bad Request");
  }

  @Test
  void shouldReturn401ProblemDetail_whenCredentialsAreInvalid() throws Exception {
    assertProblem("/unauthorized", 401, "Unauthorized");
  }

  @Test
  void shouldReturn403ProblemDetail_whenAccessIsDenied() throws Exception {
    assertProblem("/forbidden", 403, "Forbidden");
  }

  @Test
  void shouldReturn404ProblemDetail_whenResourceIsMissing() throws Exception {
    assertProblem("/not-found", 404, "Not Found");
  }

  @Test
  void shouldReturn409ProblemDetail_whenStateConflicts() throws Exception {
    assertProblem("/conflict", 409, "Conflict");
  }

  @Test
  void shouldReturn422ProblemDetail_whenAnalyticsRangeIsInvalid() throws Exception {
    assertProblem("/unprocessable", 422, "Unprocessable Entity");
  }

  private void assertProblem(String path, int status, String title) throws Exception {
    mockMvc
        .perform(get(path))
        .andExpect(status().is(status))
        .andExpect(content().contentTypeCompatibleWith(PROBLEM_JSON))
        .andExpect(jsonPath("$.type").isNotEmpty())
        .andExpect(jsonPath("$.title").value(title))
        .andExpect(jsonPath("$.status").value(status))
        .andExpect(jsonPath("$.detail").isNotEmpty())
        .andExpect(jsonPath("$.instance").value(path))
        .andExpect(jsonPath("$.data").doesNotExist());
  }

  /** Test-only endpoints that throw one representative exception each. */
  @RestController
  static class ThrowingController {

    /** Throws a 400. */
    @GetMapping("/bad-request")
    void badRequest() {
      throw new IllegalArgumentException("Invalid input");
    }

    /** Throws a 401. */
    @GetMapping("/unauthorized")
    void unauthorized() {
      throw new InvalidCredentialsException();
    }

    /** Throws a 403. */
    @GetMapping("/forbidden")
    void forbidden() {
      throw new AuthorizationDeniedException("Denied");
    }

    /** Throws a 404. */
    @GetMapping("/not-found")
    void notFound() {
      throw new OrderNotFoundException(999L);
    }

    /** Throws a 409. */
    @GetMapping("/conflict")
    void conflict() {
      throw new TableNotAvailableException(1L);
    }

    /** Throws a 422. */
    @GetMapping("/unprocessable")
    void unprocessable() {
      throw new InvalidAnalyticsRangeException("Range produced no periods");
    }
  }
}
