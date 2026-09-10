/* (C) 2026 */

package aros.services.rms.infraestructure.auth.jwk;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

/**
 * Tests for {@link JwksCachePropertiesBinding}.
 *
 * <p><b>Feature:</b> JWKS cache configuration property binding
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>should Create Bean With Valid Max Age
 *   <li>should Fail With Negative Max Age
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Verifies business logic with unit-level assertions
 * </ul>
 */
class JwksCachePropertiesBindingTest {

  private final ApplicationContextRunner runner =
      new ApplicationContextRunner()
          .withUserConfiguration(JwksCachePropertiesBindingTest.TestConfig.class);

  @Test
  void shouldCreateBeanWithValidMaxAge() {
    runner
        .withPropertyValues("app.jwt.jwks.max-age=PT2H")
        .run(
            ctx -> {
              assertThat(ctx).hasSingleBean(JwksCacheProperties.class);
              assertThat(ctx.getBean(JwksCacheProperties.class).maxAge())
                  .isEqualTo(Duration.ofHours(2));
            });
  }

  @Test
  void shouldFailWithNegativeMaxAge() {
    runner
        .withPropertyValues("app.jwt.jwks.max-age=PT-1H")
        .run(
            ctx -> {
              assertThat(ctx).hasFailed();
            });
  }

  @Configuration
  @EnableConfigurationProperties(JwksCacheProperties.class)
  static class TestConfig {}
}
