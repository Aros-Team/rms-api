/* (C) 2026 */

package aros.services.rms.infraestructure.image.api;

import aros.services.rms.core.image.domain.EntityImage;
import aros.services.rms.core.image.domain.ImageEntityType;
import aros.services.rms.core.image.domain.ImageWithUrls;
import aros.services.rms.core.image.port.input.DeleteImageUseCase;
import aros.services.rms.core.image.port.input.GetImagesUseCase;
import aros.services.rms.core.image.port.input.UploadImageUseCase;
import aros.services.rms.infraestructure.image.api.dto.ImageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Tag(name = "Images", description = "Upload, retrieve, and delete images for products and users")
public class ImageController {

  private final UploadImageUseCase uploadImageUseCase;
  private final GetImagesUseCase getImagesUseCase;
  private final DeleteImageUseCase deleteImageUseCase;

  // ==================== PRODUCT IMAGES ====================

  @Operation(
      summary = "Upload product image",
      description = "Uploads an image for a product. Accepts JPEG, PNG, or WebP up to 5MB.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Image uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file or file too large"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping(
      value = "/api/v1/products/{productId}/images",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ImageResponse> uploadProductImage(
      @Parameter(description = "Product ID", example = "42", required = true) @PathVariable
          Long productId,
      @Parameter(description = "Image file", required = true) @RequestParam("file")
          MultipartFile file)
      throws IOException {
    var image =
        uploadImageUseCase.upload(
            ImageEntityType.PRODUCT,
            productId,
            file.getOriginalFilename(),
            file.getContentType(),
            file.getBytes());
    var withUrls = getImagesUseCase.getById(image.getId());
    return new ResponseEntity<>(ImageResponse.fromDomain(withUrls), HttpStatus.CREATED);
  }

  @Operation(
      summary = "Get product images",
      description = "Retrieves all images for a product with signed URLs for each size variant.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Images retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/api/v1/products/{productId}/images")
  public ResponseEntity<List<ImageResponse>> getProductImages(
      @Parameter(description = "Product ID", example = "42", required = true) @PathVariable
          Long productId) {
    List<ImageResponse> responses =
        getImagesUseCase.getByEntity(ImageEntityType.PRODUCT, productId).stream()
            .map(ImageResponse::fromDomain)
            .collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  @Operation(
      summary = "Replace product images",
      description = "Deletes all existing images for a product and uploads new ones.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Images replaced successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid files or file too large"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PutMapping(
      value = "/api/v1/products/{productId}/images",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<List<ImageResponse>> replaceProductImages(
      @Parameter(description = "Product ID", example = "42", required = true) @PathVariable
          Long productId,
      @Parameter(description = "Image files (multiple)", required = true) @RequestParam("files")
          List<MultipartFile> files)
      throws IOException {
    deleteImageUseCase.deleteByEntity(ImageEntityType.PRODUCT, productId);
    List<ImageWithUrls> uploadedImages = new java.util.ArrayList<>();
    for (MultipartFile file : files) {
      EntityImage image =
          uploadImageUseCase.upload(
              ImageEntityType.PRODUCT,
              productId,
              file.getOriginalFilename(),
              file.getContentType(),
              file.getBytes());
      uploadedImages.add(getImagesUseCase.getById(image.getId()));
    }
    return ResponseEntity.ok(
        uploadedImages.stream().map(ImageResponse::fromDomain).collect(Collectors.toList()));
  }

  // ==================== USER IMAGES ====================

  @Operation(
      summary = "Upload user image",
      description = "Uploads a profile image for a user. Accepts JPEG, PNG, or WebP up to 5MB.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Image uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file or file too large"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping(
      value = "/api/v1/users/{userId}/images",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ImageResponse> uploadUserImage(
      @Parameter(description = "User ID", example = "1", required = true) @PathVariable Long userId,
      @Parameter(description = "Image file", required = true) @RequestParam("file")
          MultipartFile file)
      throws IOException {
    var image =
        uploadImageUseCase.upload(
            ImageEntityType.USER,
            userId,
            file.getOriginalFilename(),
            file.getContentType(),
            file.getBytes());
    var withUrls = getImagesUseCase.getById(image.getId());
    return new ResponseEntity<>(ImageResponse.fromDomain(withUrls), HttpStatus.CREATED);
  }

  @Operation(
      summary = "Get user images",
      description = "Retrieves all profile images for a user with signed URLs.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Images retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/api/v1/users/{userId}/images")
  public ResponseEntity<List<ImageResponse>> getUserImages(
      @Parameter(description = "User ID", example = "1", required = true) @PathVariable
          Long userId) {
    List<ImageResponse> responses =
        getImagesUseCase.getByEntity(ImageEntityType.USER, userId).stream()
            .map(ImageResponse::fromDomain)
            .collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  @Operation(
      summary = "Replace user images",
      description = "Deletes all existing images for a user and uploads new ones.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Images replaced successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid files or file too large"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PutMapping(
      value = "/api/v1/users/{userId}/images",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<List<ImageResponse>> replaceUserImages(
      @Parameter(description = "User ID", example = "1", required = true) @PathVariable Long userId,
      @Parameter(description = "Image files (multiple)", required = true) @RequestParam("files")
          List<MultipartFile> files)
      throws IOException {
    deleteImageUseCase.deleteByEntity(ImageEntityType.USER, userId);
    List<ImageWithUrls> uploadedImages = new java.util.ArrayList<>();
    for (MultipartFile file : files) {
      EntityImage image =
          uploadImageUseCase.upload(
              ImageEntityType.USER,
              userId,
              file.getOriginalFilename(),
              file.getContentType(),
              file.getBytes());
      uploadedImages.add(getImagesUseCase.getById(image.getId()));
    }
    return ResponseEntity.ok(
        uploadedImages.stream().map(ImageResponse::fromDomain).collect(Collectors.toList()));
  }

  // ==================== GENERIC DELETE ====================

  @Operation(
      summary = "Delete image by ID",
      description = "Permanently deletes an image and all its size variants from storage.",
      responses = {
        @ApiResponse(responseCode = "204", description = "Image deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Image not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @DeleteMapping("/api/v1/images/{imageId}")
  public ResponseEntity<Void> deleteImage(
      @Parameter(description = "Image ID", example = "1", required = true) @PathVariable
          Long imageId) {
    deleteImageUseCase.delete(imageId);
    return ResponseEntity.noContent().build();
  }
}
