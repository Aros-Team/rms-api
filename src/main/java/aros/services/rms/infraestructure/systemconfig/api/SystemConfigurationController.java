/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.api;

import aros.services.rms.core.systemconfig.domain.SystemConfiguration;
import aros.services.rms.core.systemconfig.domain.SystemConfigurationGroup;
import aros.services.rms.core.systemconfig.domain.port.input.GetSystemConfigurationUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateSystemConfigurationUseCase;
import aros.services.rms.infraestructure.systemconfig.api.dto.ConfigEntryRequest;
import aros.services.rms.infraestructure.systemconfig.api.dto.ConfigEntryResponse;
import aros.services.rms.infraestructure.systemconfig.api.dto.ConfigGroupResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for system configuration management. */
@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
@Tag(
    name = "System Configuration",
    description = "Operations for managing application configuration")
public class SystemConfigurationController {

  private final GetSystemConfigurationUseCase getSystemConfigurationUseCase;
  private final UpdateSystemConfigurationUseCase updateSystemConfigurationUseCase;

  /**
   * Returns all configuration groups with their entries.
   *
   * @return list of configuration groups with entries
   */
  @Operation(
      summary = "Get all configuration groups with entries",
      description =
          "Returns all configuration groups and their entries, sorted by group sort_order.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Configuration retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping
  public ResponseEntity<List<ConfigGroupResponse>> getAll() {
    List<SystemConfigurationGroup> groups = getSystemConfigurationUseCase.findAllGroups();
    List<SystemConfiguration> allConfigs =
        groups.stream()
            .flatMap(g -> getSystemConfigurationUseCase.findByGroupId(g.getId()).stream())
            .toList();

    Map<Long, List<SystemConfiguration>> configsByGroup =
        allConfigs.stream().collect(Collectors.groupingBy(SystemConfiguration::getGroupId));

    List<ConfigGroupResponse> responses =
        groups.stream()
            .map(
                g -> {
                  List<ConfigEntryResponse> entries =
                      configsByGroup.getOrDefault(g.getId(), List.of()).stream()
                          .map(ConfigEntryResponse::fromDomain)
                          .toList();
                  return ConfigGroupResponse.fromDomain(g, entries);
                })
            .toList();

    return ResponseEntity.ok(responses);
  }

  /**
   * Returns configuration entries for a specific group.
   *
   * @param groupId the group ID
   * @return list of configuration entries
   */
  @Operation(
      summary = "Get configuration entries by group",
      description = "Returns all configuration entries for a specific group.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Configuration entries retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/{groupId}")
  public ResponseEntity<List<ConfigEntryResponse>> getByGroup(
      @Parameter(description = "Group ID", example = "1", required = true) @PathVariable
          Long groupId) {
    List<SystemConfiguration> configs = getSystemConfigurationUseCase.findByGroupId(groupId);
    return ResponseEntity.ok(configs.stream().map(ConfigEntryResponse::fromDomain).toList());
  }

  /**
   * Updates multiple configuration entries in a single request.
   *
   * @param requests the configuration entries to update
   * @return the saved configuration entries
   */
  @Operation(
      summary = "Update configuration entries",
      description =
          "Updates multiple configuration entries in a single request. "
              + "Creates new entries if key does not exist.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Configuration updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PutMapping
  public ResponseEntity<List<ConfigEntryResponse>> update(
      @Valid @RequestBody List<ConfigEntryRequest> requests) {
    List<SystemConfiguration> configs =
        requests.stream()
            .map(req -> SystemConfiguration.builder().key(req.key()).value(req.value()).build())
            .toList();

    List<SystemConfiguration> saved = updateSystemConfigurationUseCase.saveAll(configs);
    return ResponseEntity.ok(saved.stream().map(ConfigEntryResponse::fromDomain).toList());
  }
}
