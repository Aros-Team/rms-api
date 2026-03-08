/* (C) 2026 */
package aros.services.rms.infraestructure.email.adapter;

import aros.services.rms.core.email.domain.Email;
import aros.services.rms.core.email.port.output.EmailServicePort;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EmailServiceAdapter implements EmailServicePort {
  private final RestTemplate restTemplate;
  private final String baseUrl;
  private String cachedToken;
  private Instant tokenExpiry;

  public EmailServiceAdapter(
      RestTemplate restTemplate, @Value("${app.email.base-url}") String baseUrl) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
  }

  @Override
  public void send(Email email) {
    // String token = getToken();

    // HttpHeaders headers = new HttpHeaders();
    // headers.setBearerAuth(token);

    HttpEntity<Email> request = new HttpEntity<>(email);
    restTemplate.postForEntity(baseUrl + "/api/send", request, Void.class);
  }

  private String getToken() {
    if (cachedToken != null && tokenExpiry != null && tokenExpiry.isAfter(Instant.now())) {
      return cachedToken;
    }

    ResponseEntity<TokenResponse> response =
        restTemplate.getForEntity(baseUrl + "/api/token", TokenResponse.class);

    cachedToken = response.getBody().accessToken();
    tokenExpiry = Instant.now().plusSeconds(3600);

    return cachedToken;
  }
}
