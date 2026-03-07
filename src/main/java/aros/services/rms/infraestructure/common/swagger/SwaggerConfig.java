package aros.services.rms.infraestructure.common.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Value("${app.version:1.0.0}")
  private String version;

  @Bean
  public OpenAPI rmsOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("RMS API")
                .description(
                    "Robust, scalable API that processes restaurant operations in real-time and "
                        + "transforms data into actionable business insights.")
                .version(version)
                .contact(new Contact().name("AROS Team").email("team@aros.services")));
  }
}
