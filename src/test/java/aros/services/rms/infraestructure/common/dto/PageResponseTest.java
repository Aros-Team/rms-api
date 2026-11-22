/* (C) 2026 */

package aros.services.rms.infraestructure.common.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PageResponse}.
 *
 * <p><b>Feature:</b> The single pagination envelope for page-number endpoints.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>Items live under {@code content}, never under a legacy {@code items} field
 *   <li>Metadata lives under {@code page} with {@code number}, {@code size}, {@code totalElements}
 *       and {@code totalPages}, matching Spring Data's dialect
 *   <li>{@code totalPages} is derived and never negative
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Builds a page through the factory and asserts the record components
 *   <li>Checks the zero-total and zero-size boundary cases
 * </ul>
 */
class PageResponseTest {

  @Test
  void should_build_spring_dialect_page() {
    PageResponse<String> response = PageResponse.of(List.of("a", "b"), 52, 1, 20);

    assertEquals(List.of("a", "b"), response.content());
    assertNotNull(response.page());
    assertEquals(1, response.page().number());
    assertEquals(20, response.page().size());
    assertEquals(52, response.page().totalElements());
    assertEquals(3, response.page().totalPages());
  }

  @Test
  void should_derive_zero_pages_when_empty_or_size_is_zero() {
    assertEquals(0, PageResponse.of(List.of(), 0, 0, 20).page().totalPages());
    assertEquals(0, PageResponse.of(List.of("a"), 1, 0, 0).page().totalPages());
  }
}
