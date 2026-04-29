/* (C) 2026 */
package aros.services.rms.infraestructure.email.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/** Configuration for email. */
@Configuration
public class EmailConfig {

  /**
   * Creates a RestTemplate bean.
   *
   * @return the RestTemplate
   */
  @Bean
  public RestTemplate restTemplate() {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(5000);
    factory.setReadTimeout(10000);
    return new RestTemplate(factory);
  }
}
