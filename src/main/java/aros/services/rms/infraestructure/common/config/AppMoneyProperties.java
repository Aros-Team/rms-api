package aros.services.rms.infraestructure.common.config;

import jakarta.validation.constraints.NotNull;
import java.math.RoundingMode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Configuration properties for money handling (rounding mode, etc.). */
@ConfigurationProperties(prefix = "app.money")
@Validated
public record AppMoneyProperties(@NotNull RoundingMode roundingMode) {}
