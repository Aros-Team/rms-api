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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** REST controller for generic entity image management. */
@RestController
@RequestMapping("/api/v1/{entityType}/{entityId}/images")
@RequiredArgsConstructor
@Tag(name = "Entity Images", description = "Image management for any entity type")
public class ImageController {

  private final UploadImageUseCase uploadImageUseCase;
  private final GetImagesUseCase getImagesUseCase;
  private final DeleteImageUseCase deleteImageUseCase;

  /** Uploads an image and returns the created image with signed URLs. */
  @Operation(
      summary = "Upload image",
      description = "Uploads a new image for an entity, processing it into multiple size versions.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Image uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file or oversized image"),
        @ApiResponse(responseCode = "500", description = "Image upload failed")
      })
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ImageResponse> upload(
      @Parameter(description = "Entity type (PRODUCT, USER, etc.)", example = "PRODUCT")
          @PathVariable
          ImageEntityType entityType,
      @PathVariable Long entityId,
      @RequestParam("file") MultipartFile file)
      throws IOException {
    var image =
        uploadImageUseCase.upload(
            entityType,
            entityId,
            file.getOriginalFilename(),
            file.getContentType(),
            file.getBytes());
    var withUrls = getImagesUseCase.getById(image.getId());
    return new ResponseEntity<>(ImageResponse.fromDomain(withUrls), HttpStatus.CREATED);
  }

  /** Retrieves all images for an entity with signed URLs. */
  @Operation(
      summary = "Get all entity images",
      description = "Retrieves all images for an entity with signed URLs.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Images retrieved successfully")
      })
  @GetMapping
  public ResponseEntity<List<ImageResponse>> getByEntity(
      @Parameter(description = "Entity type", example = "PRODUCT") @PathVariable
          ImageEntityType entityType,
      @PathVariable Long entityId) {
    List<ImageResponse> responses =
        getImagesUseCase.getByEntity(entityType, entityId).stream()
            .map(ImageResponse::fromDomain)
            .collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  /** Replaces all images for an entity by deleting existing ones and uploading new files. */
  @Operation(
      summary = "Replace entity images",
      description =
          "Deletes all existing images for an entity and uploads new ones. "
              + "Use this to replace all images at once.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Images replaced successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file"),
        @ApiResponse(responseCode = "500", description = "Image replacement failed")
      })
  @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<List<ImageResponse>> replace(
      @Parameter(description = "Entity type", example = "PRODUCT") @PathVariable
          ImageEntityType entityType,
      @PathVariable Long entityId,
      @RequestParam("files") List<MultipartFile> files)
      throws IOException {
    deleteImageUseCase.deleteByEntity(entityType, entityId);

    List<ImageWithUrls> uploadedImages = new java.util.ArrayList<>();
    for (MultipartFile file : files) {
      EntityImage image =
          uploadImageUseCase.upload(
              entityType,
              entityId,
              file.getOriginalFilename(),
              file.getContentType(),
              file.getBytes());
      uploadedImages.add(getImagesUseCase.getById(image.getId()));
    }

    return ResponseEntity.ok(
        uploadedImages.stream().map(ImageResponse::fromDomain).collect(Collectors.toList()));
  }

  /** Deletes a single image and all its size versions from storage. */
  @Operation(
      summary = "Delete image",
      description = "Deletes an image and all its size versions from storage.",
      responses = {
        @ApiResponse(responseCode = "204", description = "Image deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Image not found")
      })
  @DeleteMapping("/{imageId}")
  public ResponseEntity<Void> delete(
      @PathVariable ImageEntityType entityType,
      @PathVariable Long entityId,
      @PathVariable Long imageId) {
    deleteImageUseCase.delete(imageId);
    return ResponseEntity.noContent().build();
  }
}
