/* (C) 2026 */

package aros.services.rms.infraestructure.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Configuration for password encoding. */
@Configuration
public class PasswordEncoderConfig {

  /**
   * Creates a password encoder bean.
   *
   * @param encoder the BCrypt encoder
   * @return the password encoder
   */
  @Bean
  public PasswordEncoder passwordEncoder(BCryptPasswordEncoder encoder) {
    return encoder;
  }

  /**
   * Creates a BCrypt password encoder bean.
   *
   * @return the BCrypt encoder
   */
  @Bean
  public BCryptPasswordEncoder bcryptPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
