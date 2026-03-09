/* (C) 2026 */
package aros.services.rms.infraestructure.email.adapter;

import aros.services.rms.core.email.domain.Email;
import aros.services.rms.core.email.port.output.EmailServicePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EmailServiceAdapter implements EmailServicePort {
  private final RestTemplate restTemplate;
  private final String baseUrl;

  public EmailServiceAdapter(
      RestTemplate restTemplate, @Value("${app.email.base-url}") String baseUrl) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
  }

  @Override
  public void send(Email email) {
    HttpEntity<Email> request = new HttpEntity<>(email);
    restTemplate.postForEntity(baseUrl + "/api/send", request, Void.class);
  }
}
