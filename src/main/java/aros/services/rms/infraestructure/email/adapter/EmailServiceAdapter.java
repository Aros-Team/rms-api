/* (C) 2026 */
package aros.services.rms.infraestructure.email.adapter;

import aros.services.rms.core.email.application.exception.EmailServiceException;
import aros.services.rms.core.email.domain.Email;
import aros.services.rms.core.email.port.output.EmailServicePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class EmailServiceAdapter implements EmailServicePort {

  private static final Logger log = LoggerFactory.getLogger(EmailServiceAdapter.class);
  private static final int MAX_RETRIES = 3;
  private static final long[] RETRY_DELAYS_MS = {2000, 4000, 8000};

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
    RestClientException lastException = null;

    for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
      try {
        log.info(
            "Attempting to send email: attempt={}/{}, to={}, subject={}",
            attempt,
            MAX_RETRIES,
            email.getTo(),
            email.getSubject());
        restTemplate.postForEntity(baseUrl + "/api/send", request, Void.class);
        log.info("Email sent successfully: to={}, subject={}", email.getTo(), email.getSubject());
        return;
      } catch (RestClientException e) {
        lastException = e;
        log.warn(
            "Email send failed: attempt={}/{}, to={}, error={}, retrying in {}ms...",
            attempt,
            MAX_RETRIES,
            email.getTo(),
            e.getMessage(),
            RETRY_DELAYS_MS[attempt - 1]);

        if (attempt < MAX_RETRIES) {
          try {
            Thread.sleep(RETRY_DELAYS_MS[attempt - 1]);
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new EmailServiceException("Email send interrupted", ie);
          }
        }
      }
    }

    log.error(
        "Email send failed after {} attempts: to={}, subject={}, error={}",
        MAX_RETRIES,
        email.getTo(),
        email.getSubject(),
        lastException.getMessage());
    throw new EmailServiceException(
        "Failed to send email after " + MAX_RETRIES + " attempts: " + lastException.getMessage(),
        lastException);
  }
}
