/* (C) 2026 */
package aros.services.rms.infraestructure.purchase.api;

import aros.services.rms.core.purchase.domain.Supplier;
import aros.services.rms.core.purchase.port.input.CreateSupplierUseCase;
import aros.services.rms.core.purchase.port.input.GetSuppliersUseCase;
import aros.services.rms.core.purchase.port.input.UpdateSupplierUseCase;
import aros.services.rms.infraestructure.purchase.api.dto.SupplierRequest;
import aros.services.rms.infraestructure.purchase.api.dto.SupplierResponse;
import aros.services.rms.infraestructure.purchase.api.dto.SupplierUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for supplier / distributor management. */
@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
@Tag(name = "Suppliers", description = "Supplier and distributor management")
public class SupplierController {

  @Qualifier("createSupplierUseCase") private final CreateSupplierUseCase createSupplierUseCase;

  @Qualifier("updateSupplierUseCase") private final UpdateSupplierUseCase updateSupplierUseCase;

  @Qualifier("getSuppliersUseCase") private final GetSuppliersUseCase getSuppliersUseCase;

  @Operation(
      summary = "Create supplier",
      description = "Registers a new supplier. New suppliers are active by default.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Supplier created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
      })
  @PostMapping
  public ResponseEntity<SupplierResponse> create(@Valid @RequestBody SupplierRequest request) {
    var supplier = Supplier.builder().name(request.name()).contact(request.contact()).build();
    var created = createSupplierUseCase.create(supplier);
    return new ResponseEntity<>(SupplierResponse.fromDomain(created), HttpStatus.CREATED);
  }

  @Operation(
      summary = "Update supplier",
      description =
          "Updates name, contact and active status of an existing supplier."
              + " Use active=false to deactivate a supplier.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Supplier updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Supplier not found")
      })
  @PutMapping("/{id}")
  public ResponseEntity<SupplierResponse> update(
      @PathVariable Long id, @Valid @RequestBody SupplierUpdateRequest request) {
    var supplier =
        Supplier.builder()
            .name(request.name())
            .contact(request.contact())
            .active(request.active())
            .build();
    var updated = updateSupplierUseCase.update(id, supplier);
    return ResponseEntity.ok(SupplierResponse.fromDomain(updated));
  }

  @Operation(
      summary = "List all suppliers",
      description = "Returns all suppliers regardless of active status.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Suppliers retrieved successfully")
      })
  @GetMapping
  public ResponseEntity<List<SupplierResponse>> findAll() {
    List<SupplierResponse> responses =
        getSuppliersUseCase.findAll().stream()
            .map(SupplierResponse::fromDomain)
            .collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }
}
