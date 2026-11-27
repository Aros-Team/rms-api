/* (C) 2026 */

package aros.services.rms.core.systemconfig.application.service;

import aros.services.rms.core.systemconfig.domain.BusinessProfile;
import aros.services.rms.core.systemconfig.domain.SystemConfiguration;
import aros.services.rms.core.systemconfig.domain.exception.InvalidConfigurationException;
import aros.services.rms.core.systemconfig.domain.port.input.GetBusinessProfileUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateBusinessProfileUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateSystemConfigurationUseCase;
import aros.services.rms.core.systemconfig.domain.port.output.BusinessProfilePort;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Locale;
import lombok.RequiredArgsConstructor;

/** Reads and writes the business profile, keeping the legacy timezone entry in sync. */
@RequiredArgsConstructor
public class BusinessProfileService
    implements GetBusinessProfileUseCase, UpdateBusinessProfileUseCase {

  private static final String TIMEZONE_KEY = "business_timezone";

  private final BusinessProfilePort businessProfilePort;
  private final UpdateSystemConfigurationUseCase updateSystemConfigurationUseCase;

  /** {@inheritDoc} */
  @Override
  public BusinessProfile get() {
    return businessProfilePort.find().orElseGet(BusinessProfile::defaults);
  }

  /** {@inheritDoc} */
  @Override
  public BusinessProfile update(BusinessProfile profile) {
    validate(profile);
    BusinessProfile saved = businessProfilePort.save(profile);
    updateSystemConfigurationUseCase.save(
        SystemConfiguration.builder()
            .key(TIMEZONE_KEY)
            .value(saved.getTimezone())
            .description("Zona horaria del negocio")
            .build());
    return saved;
  }

  private void validate(BusinessProfile profile) {
    if (profile == null) {
      throw new InvalidConfigurationException("El perfil del negocio es obligatorio");
    }
    if (profile.getRestaurantName() == null || profile.getRestaurantName().isBlank()) {
      throw new InvalidConfigurationException("restaurantName", "nulo o vacío");
    }
    if (profile.getTimezone() == null || profile.getTimezone().isBlank()) {
      throw new InvalidConfigurationException("timezone", "nulo o vacío");
    }
    try {
      ZoneId.of(profile.getTimezone());
    } catch (DateTimeException e) {
      throw new InvalidConfigurationException(
          "Zona horaria IANA inválida: " + profile.getTimezone());
    }
    if (profile.getLocale() == null || profile.getLocale().isBlank()) {
      throw new InvalidConfigurationException("locale", "nulo o vacío");
    }
    Locale locale = Locale.forLanguageTag(profile.getLocale());
    if (locale.getLanguage().isEmpty()) {
      throw new InvalidConfigurationException(
          "Configuración regional inválida: " + profile.getLocale());
    }
  }
}
