/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.api;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.systemconfig.domain.LaborSettings;
import aros.services.rms.core.systemconfig.domain.port.input.GetBusinessProfileUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.GetCurrencySettingsUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.GetLaborSettingsUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.GetNotificationSettingsUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateBusinessProfileUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateCurrencySettingsUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateLaborSettingsUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateNotificationSettingsUseCase;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import aros.services.rms.infraestructure.share.security.JustAdminUser;
import aros.services.rms.infraestructure.systemconfig.api.dto.BusinessProfileRequest;
import aros.services.rms.infraestructure.systemconfig.api.dto.BusinessProfileResponse;
import aros.services.rms.infraestructure.systemconfig.api.dto.CurrencySettingsRequest;
import aros.services.rms.infraestructure.systemconfig.api.dto.CurrencySettingsResponse;
import aros.services.rms.infraestructure.systemconfig.api.dto.LaborSettingsRequest;
import aros.services.rms.infraestructure.systemconfig.api.dto.LaborSettingsResponse;
import aros.services.rms.infraestructure.systemconfig.api.dto.NotificationSettingsRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for typed, admin-only system settings. */
@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
@Tag(name = "Settings", description = "Typed business, currency, labor and notification settings")
@JustAdminUser
public class SettingsController {

  private final GetBusinessProfileUseCase getBusinessProfileUseCase;
  private final UpdateBusinessProfileUseCase updateBusinessProfileUseCase;
  private final GetCurrencySettingsUseCase getCurrencySettingsUseCase;
  private final UpdateCurrencySettingsUseCase updateCurrencySettingsUseCase;
  private final GetLaborSettingsUseCase getLaborSettingsUseCase;
  private final UpdateLaborSettingsUseCase updateLaborSettingsUseCase;
  private final GetNotificationSettingsUseCase getNotificationSettingsUseCase;
  private final UpdateNotificationSettingsUseCase updateNotificationSettingsUseCase;
  private final CurrencyProvider currencyProvider;

  /**
   * Returns the business profile.
   *
   * @return the stored profile or documented defaults
   */
  @Operation(
      summary = "Get business profile",
      description = "Returns the business identity, contact details, timezone and locale.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Admin access required")
      })
  @GetMapping("/business")
  public ResponseEntity<BusinessProfileResponse> getBusiness() {
    return ResponseEntity.ok(BusinessProfileResponse.fromDomain(getBusinessProfileUseCase.get()));
  }

  /**
   * Replaces the business profile.
   *
   * @param request the new profile
   * @return the persisted profile
   */
  @Operation(
      summary = "Update business profile",
      description =
          "Validates and persists the business identity. The timezone must be a valid IANA zone "
              + "and the locale a valid BCP 47 tag.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid profile data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Admin access required")
      })
  @PutMapping("/business")
  public ResponseEntity<BusinessProfileResponse> updateBusiness(
      @Valid @RequestBody BusinessProfileRequest request) {
    return ResponseEntity.ok(
        BusinessProfileResponse.fromDomain(
            updateBusinessProfileUseCase.update(request.toDomain())));
  }

  /**
   * Returns the active currency.
   *
   * @return the currency settings
   */
  @Operation(
      summary = "Get currency settings",
      description = "Returns the ISO 4217 code and formatting metadata of the system currency.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Currency retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Admin access required")
      })
  @GetMapping("/currency")
  public ResponseEntity<CurrencySettingsResponse> getCurrency() {
    return ResponseEntity.ok(CurrencySettingsResponse.fromDomain(getCurrencySettingsUseCase.get()));
  }

  /**
   * Replaces the active currency.
   *
   * @param request the new ISO 4217 code
   * @return the persisted currency settings
   */
  @Operation(
      summary = "Update currency",
      description =
          "Validates the ISO 4217 code and makes it the single source for every monetary value.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Currency updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid ISO 4217 code"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Admin access required")
      })
  @PutMapping("/currency")
  public ResponseEntity<CurrencySettingsResponse> updateCurrency(
      @Valid @RequestBody CurrencySettingsRequest request) {
    return ResponseEntity.ok(
        CurrencySettingsResponse.fromDomain(updateCurrencySettingsUseCase.update(request.code())));
  }

  /**
   * Returns labor costing settings.
   *
   * @return the labor settings
   */
  @Operation(
      summary = "Get labor settings",
      description = "Returns the labor cost mode and fallback rates.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Labor settings retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Admin access required")
      })
  @GetMapping("/labor")
  public ResponseEntity<LaborSettingsResponse> getLabor() {
    return ResponseEntity.ok(LaborSettingsRequest.toResponse(getLaborSettingsUseCase.get()));
  }

  /**
   * Replaces labor costing settings.
   *
   * @param request the new settings
   * @return the persisted settings
   */
  @Operation(
      summary = "Update labor settings",
      description =
          "Persists the labor cost mode (AUTO, REAL, STANDARD) and the fallback hourly rate.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Labor settings updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid labor settings"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Admin access required")
      })
  @PutMapping("/labor")
  public ResponseEntity<LaborSettingsResponse> updateLabor(
      @Valid @RequestBody LaborSettingsRequest request) {
    LaborSettings settings =
        new LaborSettings(
            request.mode(),
            new Money(request.standardHourlyRate(), currencyProvider.getCurrency()),
            request.defaultExpectedHoursPerMonth());
    return ResponseEntity.ok(
        LaborSettingsRequest.toResponse(updateLaborSettingsUseCase.update(settings)));
  }

  /**
   * Returns notification preferences.
   *
   * @return the notification settings
   */
  @Operation(
      summary = "Get notification preferences",
      description = "Returns the global kitchen, stock and order notification preferences.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Preferences retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Admin access required")
      })
  @GetMapping("/notifications")
  public ResponseEntity<NotificationSettingsRequest> getNotifications() {
    return ResponseEntity.ok(
        NotificationSettingsRequest.fromDomain(getNotificationSettingsUseCase.get()));
  }

  /**
   * Replaces notification preferences.
   *
   * @param request the new preferences
   * @return the persisted preferences
   */
  @Operation(
      summary = "Update notification preferences",
      description = "Persists the global kitchen sound, haptics, stock and order alert toggles.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Preferences updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid preferences"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Admin access required")
      })
  @PutMapping("/notifications")
  public ResponseEntity<NotificationSettingsRequest> updateNotifications(
      @Valid @RequestBody NotificationSettingsRequest request) {
    return ResponseEntity.ok(
        NotificationSettingsRequest.fromDomain(
            updateNotificationSettingsUseCase.update(request.toDomain())));
  }
}
