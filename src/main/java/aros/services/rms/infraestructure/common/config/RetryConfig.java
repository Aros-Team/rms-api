/* (C) 2026 */
package aros.services.rms.infraestructure.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry(proxyTargetClass = true)
public class RetryConfig {}
