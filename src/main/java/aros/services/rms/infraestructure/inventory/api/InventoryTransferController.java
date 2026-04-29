/* (C) 2026 */
package aros.services.rms.infraestructure.inventory.api;

import aros.services.rms.core.inventory.port.input.TransferInventoryUseCase;
import aros.services.rms.infraestructure.inventory.api.dto.TransferRequest;
import aros.services.rms.infraestructure.inventory.api.dto.TransferResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for inventory transfer operations. */
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory Transfer", description = "Transfer supplies between storage locations")
public class InventoryTransferController {

  private final TransferInventoryUseCase transferInventoryUseCase;

  /**
   * Transfers supplies from Bodega to Cocina.
   *
   * @param request the transfer request
   * @return the list of transfers
   */
  @Operation(
      summary = "Transfer supplies from Bodega to Cocina",
      description =
          "Transfers multiple supply variants from Bodega to Cocina. "
              + "Operation is atomic: all transfers succeed or all fail. "
              + "Validates that variants exist and have sufficient stock in Bodega.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Transfer completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or insufficient stock"),
        @ApiResponse(responseCode = "404", description = "Variant or storage location not found")
      })
  @PostMapping("/transfer")
  @Transactional
  public ResponseEntity<List<TransferResponse>> transferToKitchen(
      @Valid @RequestBody TransferRequest request) {

    // Convert DTOs to domain transfer items
    List<TransferInventoryUseCase.TransferItem> items =
        request.items().stream()
            .map(
                item ->
                    new TransferInventoryUseCase.TransferItem(
                        item.supplyVariantId(), item.quantity()))
            .collect(Collectors.toList());

    // Execute transfer use case
    var movements = transferInventoryUseCase.transferToKitchen(items);

    // Convert domain movements to response DTOs
    List<TransferResponse> responses =
        movements.stream().map(TransferResponse::fromDomain).collect(Collectors.toList());

    return ResponseEntity.status(HttpStatus.OK).body(responses);
  }
}
