package aros.services.rms.core.image.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Entity type that owns the image")
public enum ImageEntityType {
  PRODUCT,
  USER
}
