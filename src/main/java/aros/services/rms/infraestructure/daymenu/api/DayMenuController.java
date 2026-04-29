/* (C) 2026 */

package aros.services.rms.infraestructure.daymenu.api;

import aros.services.rms.core.daymenu.application.exception.UnauthenticatedOperationException;
import aros.services.rms.core.daymenu.port.input.GetCurrentDayMenuUseCase;
import aros.services.rms.core.daymenu.port.input.GetDayMenuHistoryUseCase;
import aros.services.rms.core.daymenu.port.input.UpdateDayMenuUseCase;
import aros.services.rms.infraestructure.daymenu.api.dto.DayMenuHistoryResponse;
import aros.services.rms.infraestructure.daymenu.api.dto.DayMenuResponse;
import aros.services.rms.infraestructure.daymenu.api.dto.UpdateDayMenuRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for day menu management. */
@RestController
@RequestMapping("/api/v1/day-menu")
@RequiredArgsConstructor
@Tag(name = "Day Menu", description = "Gestión del menú del día y su historial")
public class DayMenuController {

  private final UpdateDayMenuUseCase updateDayMenuUseCase;
  private final GetCurrentDayMenuUseCase getCurrentDayMenuUseCase;
  private final GetDayMenuHistoryUseCase getDayMenuHistoryUseCase;

  /**
   * Updates the day menu.
   *
   * @param request the update request
   * @return the updated day menu
   */
  @Operation(
      summary = "Actualizar el menú del día",
      description =
          "Establece un nuevo producto como menú del día. El producto debe tener hasOptions=true. "
              + "El menú anterior se archiva automáticamente con su fecha de vigencia.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Menú del día actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Producto inválido o datos incorrectos"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
      })
  @PutMapping
  public ResponseEntity<DayMenuResponse> update(@Valid @RequestBody UpdateDayMenuRequest request) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
      throw new UnauthenticatedOperationException();
    }
    String createdBy = auth.getName();
    var dayMenu = updateDayMenuUseCase.update(request.productId(), createdBy);
    return ResponseEntity.ok(DayMenuResponse.fromDomain(dayMenu));
  }

  /**
   * Gets the current day menu.
   *
   * @return the current day menu or no content
   */
  @Operation(
      summary = "Obtener el menú del día actual",
      description =
          "Retorna el menú del día activo. Si no hay ninguno configurado, retorna 204 No Content.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Menú del día activo encontrado"),
        @ApiResponse(responseCode = "204", description = "No hay menú del día configurado"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
      })
  @GetMapping("/current")
  public ResponseEntity<DayMenuResponse> getCurrent() {
    return getCurrentDayMenuUseCase
        .getCurrent()
        .map(DayMenuResponse::fromDomain)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.noContent().build());
  }

  /**
   * Gets the day menu history.
   *
   * @param page the page number
   * @param size the page size
   * @return the page of day menu history
   */
  @Operation(
      summary = "Obtener historial del menú del día",
      description =
          "Retorna el historial paginado de menús del día anteriores, ordenado por fecha de "
              + "vigencia descendente. page >= 0, size entre 1 y 100.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Historial obtenido exitosamente"),
        @ApiResponse(responseCode = "400", description = "Parámetros de paginación inválidos"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
      })
  @GetMapping("/history")
  public ResponseEntity<Page<DayMenuHistoryResponse>> getHistory(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    if (page < 0) {
      throw new IllegalArgumentException("El parámetro page debe ser mayor o igual a 0");
    }
    if (size <= 0 || size > 100) {
      throw new IllegalArgumentException("El parámetro size debe estar entre 1 y 100");
    }
    var pageable = PageRequest.of(page, size);
    var result =
        getDayMenuHistoryUseCase.getHistory(pageable).map(DayMenuHistoryResponse::fromDomain);
    return ResponseEntity.ok(result);
  }
}
