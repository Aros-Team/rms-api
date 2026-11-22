/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.api;

import aros.services.rms.core.systemconfig.domain.SystemConfiguration;
import aros.services.rms.core.systemconfig.domain.SystemConfigurationGroup;
import aros.services.rms.core.systemconfig.domain.exception.ConfigGroupNotFoundException;
import aros.services.rms.core.systemconfig.domain.port.input.GetSystemConfigurationUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.ManageConfigurationUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateSystemConfigurationUseCase;
import aros.services.rms.infraestructure.share.security.JustAdminUser;
import aros.services.rms.infraestructure.systemconfig.api.dto.ConfigEntryRequest;
import aros.services.rms.infraestructure.systemconfig.api.dto.ConfigEntryResponse;
import aros.services.rms.infraestructure.systemconfig.api.dto.ConfigEntryUpdateRequest;
import aros.services.rms.infraestructure.systemconfig.api.dto.ConfigGroupRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
@JustAdminUser
public class SystemConfigurationController {

  private final GetSystemConfigurationUseCase getSystemConfigurationUseCase;
  private final UpdateSystemConfigurationUseCase updateSystemConfigurationUseCase;
  private final ManageConfigurationUseCase manageConfigurationUseCase;

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
   * Returns configuration entries for a group identified by numeric id or name.
   *
   * @param groupRef the group id or name
   * @return list of configuration entries
   */
  @Operation(
      summary = "Get configuration entries by group",
      description =
          "Returns all configuration entries for a group. The path accepts a numeric group id "
              + "or the group name (for example general).",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Configuration entries retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Group not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/{groupRef}")
  public ResponseEntity<List<ConfigEntryResponse>> getByGroup(
      @Parameter(description = "Group ID or name", example = "general", required = true)
          @PathVariable
          String groupRef) {
    Long groupId = resolveGroupId(groupRef);
    List<SystemConfiguration> configs = getSystemConfigurationUseCase.findByGroupId(groupId);
    return ResponseEntity.ok(configs.stream().map(ConfigEntryResponse::fromDomain).toList());
  }

  private Long resolveGroupId(String groupRef) {
    if (groupRef.chars().allMatch(Character::isDigit)) {
      return Long.valueOf(groupRef);
    }
    return getSystemConfigurationUseCase.findAllGroups().stream()
        .filter(group -> group.getName().equalsIgnoreCase(groupRef))
        .map(SystemConfigurationGroup::getId)
        .findFirst()
        .orElseThrow(() -> new ConfigGroupNotFoundException(groupRef));
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
            .map(
                req ->
                    SystemConfiguration.builder()
                        .key(req.key())
                        .value(req.value())
                        .groupId(req.groupId())
                        .sortOrder(req.sortOrder())
                        .build())
            .toList();

    List<SystemConfiguration> saved = updateSystemConfigurationUseCase.saveAll(configs);
    return ResponseEntity.ok(saved.stream().map(ConfigEntryResponse::fromDomain).toList());
  }

  /**
   * Creates a configuration group.
   *
   * @param request the group data
   * @return the created group with 201 status
   */
  @Operation(
      summary = "Create configuration group",
      description = "Creates a new configuration group. Names are unique.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Group created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid group data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "409", description = "Group name already exists")
      })
  @PostMapping("/groups")
  public ResponseEntity<ConfigGroupResponse> createGroup(
      @Valid @RequestBody ConfigGroupRequest request) {
    SystemConfigurationGroup created =
        manageConfigurationUseCase.createGroup(
            new ManageConfigurationUseCase.GroupCommand(
                request.name(), request.description(), request.sortOrder()));
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ConfigGroupResponse.fromDomain(created, List.of()));
  }

  /**
   * Renames, re-describes or reorders a configuration group.
   *
   * @param id the group id
   * @param request the new group data
   * @return the updated group
   */
  @Operation(
      summary = "Update configuration group",
      description = "Renames, re-describes or reorders a configuration group.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Group updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid group data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Group not found"),
        @ApiResponse(responseCode = "409", description = "Group name already exists")
      })
  @PutMapping("/groups/{id}")
  public ResponseEntity<ConfigGroupResponse> updateGroup(
      @Parameter(description = "Group ID", example = "1", required = true) @PathVariable Long id,
      @Valid @RequestBody ConfigGroupRequest request) {
    SystemConfigurationGroup updated =
        manageConfigurationUseCase.updateGroup(
            id,
            new ManageConfigurationUseCase.GroupCommand(
                request.name(), request.description(), request.sortOrder()));
    List<ConfigEntryResponse> entries =
        getSystemConfigurationUseCase.findByGroupId(id).stream()
            .map(ConfigEntryResponse::fromDomain)
            .toList();
    return ResponseEntity.ok(ConfigGroupResponse.fromDomain(updated, entries));
  }

  /**
   * Deletes an empty configuration group.
   *
   * @param id the group id
   * @return empty response with 204 status
   */
  @Operation(
      summary = "Delete configuration group",
      description = "Deletes a configuration group that has no entries.",
      responses = {
        @ApiResponse(responseCode = "204", description = "Group deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Group not found"),
        @ApiResponse(responseCode = "409", description = "Group still has entries")
      })
  @DeleteMapping("/groups/{id}")
  public ResponseEntity<Void> deleteGroup(
      @Parameter(description = "Group ID", example = "1", required = true) @PathVariable Long id) {
    manageConfigurationUseCase.deleteGroup(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Updates a single configuration entry.
   *
   * @param key the configuration key
   * @param request the partial update
   * @return the saved entry
   */
  @Operation(
      summary = "Update one configuration entry",
      description =
          "Partially updates an existing entry. Use PUT /config for the legacy bulk upsert.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Entry updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid entry data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Entry not found")
      })
  @PatchMapping("/{key}")
  public ResponseEntity<ConfigEntryResponse> updateEntry(
      @Parameter(description = "Configuration key", example = "default_currency", required = true)
          @PathVariable
          String key,
      @Valid @RequestBody ConfigEntryUpdateRequest request) {
    SystemConfiguration saved =
        manageConfigurationUseCase.updateEntry(
            key,
            new ManageConfigurationUseCase.EntryUpdate(
                request.value(), request.description(), request.groupId(), request.sortOrder()));
    return ResponseEntity.ok(ConfigEntryResponse.fromDomain(saved));
  }

  /**
   * Deletes a single configuration entry.
   *
   * @param key the configuration key
   * @return empty response with 204 status
   */
  @Operation(
      summary = "Delete one configuration entry",
      description = "Deletes an existing configuration entry by key.",
      responses = {
        @ApiResponse(responseCode = "204", description = "Entry deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Entry not found")
      })
  @DeleteMapping("/{key}")
  public ResponseEntity<Void> deleteEntry(
      @Parameter(description = "Configuration key", example = "legacy_key", required = true)
          @PathVariable
          String key) {
    manageConfigurationUseCase.deleteEntry(key);
    return ResponseEntity.noContent().build();
  }
}
