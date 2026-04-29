/* (C) 2026 */

package aros.services.rms.core.email.domain;

import java.util.Map;

/** Domain model representing an email message to be sent. */
public class Email {
  private String to;
  private String subject;
  private String template;
  private Map<String, Object> data;

  /**
   * Creates an email.
   *
   * @param to recipient address
   * @param subject email subject
   * @param template email template
   * @param data template data
   */
  public Email(String to, String subject, String template, Map<String, Object> data) {
    this.to = to;
    this.subject = subject;
    this.template = template;
    this.data = data;
  }

  /**
   * Changes the destination of this email.
   *
   * @param newDestination new recipient address
   */
  public void changeDestination(String newDestination) {
    this.to = newDestination;
  }

  public String getTo() {
    return to;
  }

  public String getSubject() {
    return subject;
  }

  public String getTemplate() {
    return template;
  }

  public Map<String, Object> getData() {
    return data;
  }
}
