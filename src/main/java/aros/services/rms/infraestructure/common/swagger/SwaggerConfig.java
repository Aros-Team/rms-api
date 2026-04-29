package aros.services.rms.infraestructure.common.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuration for Swagger/OpenAPI documentation. */
@Configuration
@ConditionalOnProperty(
    name = "springdoc.swagger-ui.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class SwaggerConfig {

  @Value("${app.version:1.0.0}")
  private String version;

  /**
   * Creates the OpenAPI configuration.
   *
   * @return the OpenAPI configuration
   */
  @Bean
  public OpenAPI rmsOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("RMS API")
                .description(
                    "Robust, scalable API that processes restaurant operations in real-time and "
                        + "transforms data into actionable business insights.")
                .version(version)
                .contact(new Contact().name("AROS Team").email("team@aros.services")))
        .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
        .components(
            new Components()
                .addSecuritySchemes(
                    "Bearer Authentication",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
  }
}
