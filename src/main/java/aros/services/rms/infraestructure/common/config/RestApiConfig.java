/* (C) 2026 */

package aros.services.rms.infraestructure.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Global REST API configuration. Ensures all endpoints produce application/json by default. */
@Configuration
public class RestApiConfig implements WebMvcConfigurer {

  @Override
  public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    configurer.defaultContentType(org.springframework.http.MediaType.APPLICATION_JSON);
  }
}
