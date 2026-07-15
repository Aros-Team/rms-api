/* (C) 2026 */

package aros.services.rms.infraestructure.auth.jwk;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

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
