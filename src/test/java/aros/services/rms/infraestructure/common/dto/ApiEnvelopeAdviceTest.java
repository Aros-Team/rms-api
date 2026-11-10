/* (C) 2026 */

package aros.services.rms.infraestructure.common.dto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tests for {@link ApiEnvelopeAdvice}.
 *
 * <p><b>Feature:</b> Automatic wrapping of successful REST payloads in {@link ApiResponse}.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>200 JSON DTO responses are wrapped as {@code {data:...}}
 *   <li>204 responses stay empty
 *   <li>4xx {@link ProblemDetail} responses are never wrapped
 *   <li>bodies that are already {@link ApiResponse} are not wrapped twice
 *   <li>non-JSON responses are left untouched
 *   <li>{@code application/jwk-set+json} and {@code /.well-known} responses are never wrapped
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Builds a standalone MockMvc with the advice registered and a tiny test controller
 *   <li>Performs real HTTP requests and asserts the rendered JSON/status
 * </ul>
 */
class ApiEnvelopeAdviceTest {

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new EnvelopeTestController(), new WellKnownTestController())
            .setControllerAdvice(new ApiEnvelopeAdvice())
            .build();
  }

  @Test
  void should_wrap_success_dto_in_data_envelope() throws Exception {
    mockMvc
        .perform(get("/api/test/envelope/dto"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.name").value("ok"))
        .andExpect(jsonPath("$.meta").doesNotExist());
  }

  @Test
  void should_keep_no_content_response_empty() throws Exception {
    mockMvc
        .perform(get("/api/test/envelope/no-content"))
        .andExpect(status().isNoContent())
        .andExpect(content().string(""));
  }

  @Test
  void should_not_wrap_client_error_problem_detail() throws Exception {
    mockMvc
        .perform(get("/api/test/envelope/problem"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("nope"))
        .andExpect(jsonPath("$.data").doesNotExist());
  }

  @Test
  void should_not_double_wrap_existing_api_response() throws Exception {
    mockMvc
        .perform(get("/api/test/envelope/already-wrapped"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.name").value("ok"))
        .andExpect(jsonPath("$.data.data").doesNotExist());
  }

  @Test
  void should_not_wrap_non_json_response() throws Exception {
    mockMvc
        .perform(get("/api/test/envelope/text"))
        .andExpect(status().isOk())
        .andExpect(content().string("plain"));
  }

  @Test
  void should_not_wrap_jwk_set_media_type() throws Exception {
    mockMvc
        .perform(get("/api/test/envelope/jwks").accept("application/jwk-set+json"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.keys[0].kty").value("RSA"))
        .andExpect(jsonPath("$.data").doesNotExist());
  }

  @Test
  void should_not_wrap_well_known_path_even_when_json() throws Exception {
    mockMvc
        .perform(get("/.well-known/jwks.json"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.keys[0].kty").value("RSA"))
        .andExpect(jsonPath("$.data").doesNotExist());
  }

  private record TestPayload(String name) {}

  @RestController
  @RequestMapping("/api/test/envelope")
  static class EnvelopeTestController {

    @GetMapping("/dto")
    TestPayload dto() {
      return new TestPayload("ok");
    }

    @GetMapping(value = "/jwks", produces = "application/jwk-set+json")
    java.util.Map<String, Object> jwks() {
      return java.util.Map.of("keys", java.util.List.of(java.util.Map.of("kty", "RSA")));
    }

    @GetMapping("/no-content")
    ResponseEntity<Void> noContent() {
      return ResponseEntity.noContent().build();
    }

    @GetMapping("/problem")
    ResponseEntity<ProblemDetail> problem() {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "nope"));
    }

    @GetMapping("/already-wrapped")
    ApiResponse<TestPayload> alreadyWrapped() {
      return ApiResponse.of(new TestPayload("ok"));
    }

    @GetMapping(value = "/text", produces = "text/plain")
    String text() {
      return "plain";
    }
  }

  @RestController
  @RequestMapping("/.well-known")
  static class WellKnownTestController {

    @GetMapping(value = "/jwks.json", produces = "application/json")
    java.util.Map<String, Object> jwks() {
      return java.util.Map.of("keys", java.util.List.of(java.util.Map.of("kty", "RSA")));
    }
  }
}
