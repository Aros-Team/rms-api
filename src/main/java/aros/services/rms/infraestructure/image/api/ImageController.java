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

/** REST controller exposing endpoints to manage images owned by products or users. */
@RestController
@RequiredArgsConstructor
public class ImageController {

  private final UploadImageUseCase uploadImageUseCase;
  private final GetImagesUseCase getImagesUseCase;
  private final DeleteImageUseCase deleteImageUseCase;

  // ==================== PRODUCT IMAGES ====================

  /**
   * Uploads a single image for a product.
   *
   * @param productId target product ID
   * @param file multipart file to upload
   * @return the uploaded image with its size-variant URLs
   */
  @Operation(
      tags = {"Products"},
      summary = "Upload product image",
      description = "Uploads an image for a product. Accepts JPEG, PNG, or WebP up to 5MB.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Image uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file or file too large"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping(
      value = "/api/v1/products/{productId}/images",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ImageResponse> uploadProductImage(
      @Parameter(description = "Product ID", example = "42", required = true) @PathVariable
          Long productId,
      @Parameter(
              description = "Image file (JPEG, PNG, WebP up to 5MB)",
              example = "burger.jpg",
              required = true)
          @RequestParam("file")
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

  /**
   * Lists all images attached to a product.
   *
   * @param productId target product ID
   * @return list of images with size-variant URLs
   */
  @Operation(
      tags = {"Products"},
      summary = "Get product images",
      description = "Retrieves all images for a product with signed URLs for each size variant.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Images retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
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

  /**
   * Replaces all images of a product with the provided set.
   *
   * @param productId target product ID
   * @param files new image files to upload
   * @return the new set of images with size-variant URLs
   */
  @Operation(
      tags = {"Products"},
      summary = "Replace product images",
      description = "Deletes all existing images for a product and uploads new ones.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Images replaced successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid files or file too large"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PutMapping(
      value = "/api/v1/products/{productId}/images",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<List<ImageResponse>> replaceProductImages(
      @Parameter(description = "Product ID", example = "42", required = true) @PathVariable
          Long productId,
      @Parameter(
              description = "Image files (multiple, JPEG/PNG/WebP up to 5MB each)",
              example = "[\"burger.jpg\", \"drink.jpg\"]",
              required = true)
          @RequestParam("files")
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

  // ==================== WORKER IMAGES ====================

  /**
   * Uploads a single profile image for a worker.
   *
   * @param workerId target worker ID
   * @param file multipart file to upload
   * @return the uploaded image with its size-variant URLs
   */
  @Operation(
      tags = {"Workers"},
      summary = "Upload worker image",
      description = "Uploads a profile image for a worker. Accepts JPEG, PNG, or WebP up to 5MB.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Image uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file or file too large"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Worker not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping(
      value = "/api/v1/workers/{workerId}/images",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ImageResponse> uploadWorkerImage(
      @Parameter(description = "Worker ID", example = "1", required = true) @PathVariable
          Long workerId,
      @Parameter(
              description = "Image file (JPEG, PNG, WebP up to 5MB)",
              example = "profile.jpg",
              required = true)
          @RequestParam("file")
          MultipartFile file)
      throws IOException {
    var image =
        uploadImageUseCase.upload(
            ImageEntityType.USER,
            workerId,
            file.getOriginalFilename(),
            file.getContentType(),
            file.getBytes());
    var withUrls = getImagesUseCase.getById(image.getId());
    return new ResponseEntity<>(ImageResponse.fromDomain(withUrls), HttpStatus.CREATED);
  }

  /**
   * Lists all profile images for a worker.
   *
   * @param workerId target worker ID
   * @return list of images with size-variant URLs
   */
  @Operation(
      tags = {"Workers"},
      summary = "Get worker images",
      description = "Retrieves all profile images for a worker with signed URLs.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Images retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Worker not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/api/v1/workers/{workerId}/images")
  public ResponseEntity<List<ImageResponse>> getWorkerImages(
      @Parameter(description = "Worker ID", example = "1", required = true) @PathVariable
          Long workerId) {
    List<ImageResponse> responses =
        getImagesUseCase.getByEntity(ImageEntityType.USER, workerId).stream()
            .map(ImageResponse::fromDomain)
            .collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  /**
   * Replaces all profile images of a worker with the provided set.
   *
   * @param workerId target worker ID
   * @param files new image files to upload
   * @return the new set of images with size-variant URLs
   */
  @Operation(
      tags = {"Workers"},
      summary = "Replace worker images",
      description = "Deletes all existing images for a worker and uploads new ones.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Images replaced successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid files or file too large"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Worker not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PutMapping(
      value = "/api/v1/workers/{workerId}/images",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<List<ImageResponse>> replaceWorkerImages(
      @Parameter(description = "Worker ID", example = "1", required = true) @PathVariable
          Long workerId,
      @Parameter(
              description = "Image files (multiple, JPEG/PNG/WebP up to 5MB each)",
              example = "[\"profile1.jpg\", \"profile2.jpg\"]",
              required = true)
          @RequestParam("files")
          List<MultipartFile> files)
      throws IOException {
    deleteImageUseCase.deleteByEntity(ImageEntityType.USER, workerId);
    List<ImageWithUrls> uploadedImages = new java.util.ArrayList<>();
    for (MultipartFile file : files) {
      EntityImage image =
          uploadImageUseCase.upload(
              ImageEntityType.USER,
              workerId,
              file.getOriginalFilename(),
              file.getContentType(),
              file.getBytes());
      uploadedImages.add(getImagesUseCase.getById(image.getId()));
    }
    return ResponseEntity.ok(
        uploadedImages.stream().map(ImageResponse::fromDomain).collect(Collectors.toList()));
  }

  // ==================== GENERIC DELETE ====================

  /**
   * Deletes an image and all its size variants by ID.
   *
   * @param imageId target image ID
   * @return empty response with 204 status
   */
  @Operation(
      tags = {"Images"},
      summary = "Delete image by ID",
      description = "Permanently deletes an image and all its size variants from storage.",
      responses = {
        @ApiResponse(responseCode = "204", description = "Image deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
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
