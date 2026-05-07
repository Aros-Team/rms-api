/* (C) 2026 */

package aros.services.rms.infraestructure.image.api;

import aros.services.rms.core.image.port.input.DeleteProductImageUseCase;
import aros.services.rms.core.image.port.input.GetProductImagesUseCase;
import aros.services.rms.core.image.port.input.UploadProductImageUseCase;
import aros.services.rms.infraestructure.image.api.dto.ImageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** REST controller for product image management. */
@RestController
@RequestMapping("/api/v1/products/{productId}/images")
@RequiredArgsConstructor
@Tag(name = "Product Images", description = "Product image management")
public class ImageController {

  private final UploadProductImageUseCase uploadProductImageUseCase;
  private final GetProductImagesUseCase getProductImagesUseCase;
  private final DeleteProductImageUseCase deleteProductImageUseCase;

  /** Uploads a product image and returns the created image with signed URLs. */
  @Operation(
      summary = "Upload product image",
      description = "Uploads a new image for a product, processing it into multiple size versions.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Image uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file or oversized image"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Image upload failed")
      })
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ImageResponse> upload(
      @PathVariable Long productId, @RequestParam("file") MultipartFile file) throws IOException {
    var image =
        uploadProductImageUseCase.upload(
            productId, file.getOriginalFilename(), file.getContentType(), file.getBytes());
    var withUrls = getProductImagesUseCase.getById(image.getId());
    return new ResponseEntity<>(ImageResponse.fromDomain(withUrls), HttpStatus.CREATED);
  }

  /** Retrieves all images for a product with signed URLs. */
  @Operation(
      summary = "Get all product images",
      description = "Retrieves all images for a product with signed URLs.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Images retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found")
      })
  @GetMapping
  public ResponseEntity<List<ImageResponse>> getByProductId(@PathVariable Long productId) {
    List<ImageResponse> responses =
        getProductImagesUseCase.getByProductId(productId).stream()
            .map(ImageResponse::fromDomain)
            .collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  /** Deletes a product image and all its size versions. */
  @Operation(
      summary = "Delete product image",
      description = "Deletes a product image and all its size versions from storage.",
      responses = {
        @ApiResponse(responseCode = "204", description = "Image deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid image ID"),
        @ApiResponse(responseCode = "404", description = "Image not found")
      })
  @DeleteMapping("/{imageId}")
  public ResponseEntity<Void> delete(@PathVariable Long productId, @PathVariable Long imageId) {
    deleteProductImageUseCase.delete(imageId);
    return ResponseEntity.noContent().build();
  }
}
