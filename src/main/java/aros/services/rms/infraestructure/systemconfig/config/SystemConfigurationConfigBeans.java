/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.config;

import aros.services.rms.core.systemconfig.application.service.BusinessProfileService;
import aros.services.rms.core.systemconfig.application.service.CurrencySettingsService;
import aros.services.rms.core.systemconfig.application.service.GetSystemConfigurationService;
import aros.services.rms.core.systemconfig.application.service.LaborSettingsService;
import aros.services.rms.core.systemconfig.application.service.ManageConfigurationService;
import aros.services.rms.core.systemconfig.application.service.NotificationSettingsService;
import aros.services.rms.core.systemconfig.application.service.UpdateSystemConfigurationService;
import aros.services.rms.core.systemconfig.domain.port.input.GetBusinessProfileUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.GetCurrencySettingsUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.GetLaborSettingsUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.GetNotificationSettingsUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.GetSystemConfigurationUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.ManageConfigurationUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateBusinessProfileUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateCurrencySettingsUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateLaborSettingsUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateNotificationSettingsUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateSystemConfigurationUseCase;
import aros.services.rms.core.systemconfig.domain.port.output.BusinessProfilePort;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import aros.services.rms.core.systemconfig.domain.port.output.NotificationPreferencesPort;
import aros.services.rms.core.systemconfig.domain.port.output.SystemConfigurationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuration of beans for the system configuration module. */
@Configuration
@RequiredArgsConstructor
public class SystemConfigurationConfigBeans {

  private final GetSystemConfigurationService getSystemConfigurationService;
  private final UpdateSystemConfigurationService updateSystemConfigurationService;

  /** Creates bean for system configuration query use case. */
  @Bean
  public GetSystemConfigurationUseCase getSystemConfigurationUseCase() {
    return getSystemConfigurationService;
  }

  /** Creates bean for system configuration update use case. */
  @Bean
  public UpdateSystemConfigurationUseCase updateSystemConfigurationUseCase() {
    return updateSystemConfigurationService;
  }

  /**
   * Creates the business profile read and write services.
   *
   * @param businessProfilePort the business profile port
   * @param updateSystemConfigurationUseCase the configuration upsert use case
   * @return the business profile service
   */
  @Bean
  public BusinessProfileService businessProfileService(
      BusinessProfilePort businessProfilePort,
      UpdateSystemConfigurationUseCase updateSystemConfigurationUseCase) {
    return new BusinessProfileService(businessProfilePort, updateSystemConfigurationUseCase);
  }

  /**
   * Exposes the business profile read use case.
   *
   * @param service the business profile service
   * @return the read use case
   */
  @Bean
  public GetBusinessProfileUseCase getBusinessProfileUseCase(
      @Qualifier("businessProfileService") BusinessProfileService service) {
    return service;
  }

  /**
   * Exposes the business profile write use case.
   *
   * @param service the business profile service
   * @return the write use case
   */
  @Bean
  public UpdateBusinessProfileUseCase updateBusinessProfileUseCase(
      @Qualifier("businessProfileService") BusinessProfileService service) {
    return service;
  }

  /**
   * Creates the currency settings service.
   *
   * @param currencyProvider the currency provider
   * @param updateSystemConfigurationUseCase the configuration upsert use case
   * @param eventPublisher the event publisher used to refresh currency caches
   * @return the currency service
   */
  @Bean
  public CurrencySettingsService currencySettingsService(
      CurrencyProvider currencyProvider,
      UpdateSystemConfigurationUseCase updateSystemConfigurationUseCase,
      ApplicationEventPublisher eventPublisher) {
    return new CurrencySettingsService(
        currencyProvider, updateSystemConfigurationUseCase, eventPublisher);
  }

  /**
   * Exposes the currency read use case.
   *
   * @param service the currency service
   * @return the read use case
   */
  @Bean
  public GetCurrencySettingsUseCase getCurrencySettingsUseCase(
      @Qualifier("currencySettingsService") CurrencySettingsService service) {
    return service;
  }

  /**
   * Exposes the currency write use case.
   *
   * @param service the currency service
   * @return the write use case
   */
  @Bean
  public UpdateCurrencySettingsUseCase updateCurrencySettingsUseCase(
      @Qualifier("currencySettingsService") CurrencySettingsService service) {
    return service;
  }

  /**
   * Creates the labor settings service.
   *
   * @param getSystemConfigurationUseCase the configuration read use case
   * @param updateSystemConfigurationUseCase the configuration upsert use case
   * @param currencyProvider the currency provider
   * @return the labor settings service
   */
  @Bean
  public LaborSettingsService laborSettingsService(
      GetSystemConfigurationUseCase getSystemConfigurationUseCase,
      UpdateSystemConfigurationUseCase updateSystemConfigurationUseCase,
      CurrencyProvider currencyProvider) {
    return new LaborSettingsService(
        getSystemConfigurationUseCase, updateSystemConfigurationUseCase, currencyProvider);
  }

  /**
   * Exposes the labor settings read use case.
   *
   * @param service the labor settings service
   * @return the read use case
   */
  @Bean
  public GetLaborSettingsUseCase getLaborSettingsUseCase(
      @Qualifier("laborSettingsService") LaborSettingsService service) {
    return service;
  }

  /**
   * Exposes the labor settings write use case.
   *
   * @param service the labor settings service
   * @return the write use case
   */
  @Bean
  public UpdateLaborSettingsUseCase updateLaborSettingsUseCase(
      @Qualifier("laborSettingsService") LaborSettingsService service) {
    return service;
  }

  /**
   * Creates the notification settings service.
   *
   * @param port the notification preferences port
   * @return the notification settings service
   */
  @Bean
  public NotificationSettingsService notificationSettingsService(NotificationPreferencesPort port) {
    return new NotificationSettingsService(port);
  }

  /**
   * Exposes the notification read use case.
   *
   * @param service the notification settings service
   * @return the read use case
   */
  @Bean
  public GetNotificationSettingsUseCase getNotificationSettingsUseCase(
      @Qualifier("notificationSettingsService") NotificationSettingsService service) {
    return service;
  }

  /**
   * Exposes the notification write use case.
   *
   * @param service the notification settings service
   * @return the write use case
   */
  @Bean
  public UpdateNotificationSettingsUseCase updateNotificationSettingsUseCase(
      @Qualifier("notificationSettingsService") NotificationSettingsService service) {
    return service;
  }

  /**
   * Creates the configuration group and entry management service.
   *
   * @param port the configuration persistence port
   * @param updateSystemConfigurationUseCase the configuration upsert use case
   * @return the management use case
   */
  @Bean
  public ManageConfigurationUseCase manageConfigurationUseCase(
      SystemConfigurationPort port,
      UpdateSystemConfigurationUseCase updateSystemConfigurationUseCase) {
    return new ManageConfigurationService(port, updateSystemConfigurationUseCase);
  }
}
