/* (C) 2026 */

package aros.services.rms.infraestructure.image.config;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.image.application.service.DeleteProductImageService;
import aros.services.rms.core.image.application.service.GetProductImagesService;
import aros.services.rms.core.image.application.service.UploadProductImageService;
import aros.services.rms.core.image.port.input.DeleteProductImageUseCase;
import aros.services.rms.core.image.port.input.GetProductImagesUseCase;
import aros.services.rms.core.image.port.input.UploadProductImageUseCase;
import aros.services.rms.core.image.port.output.ImageProcessingPort;
import aros.services.rms.core.image.port.output.ImageRepositoryPort;
import aros.services.rms.core.image.port.output.StoragePort;
import aros.services.rms.infraestructure.image.processing.ScrimeImageProcessor;
import aros.services.rms.infraestructure.image.storage.StorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuration of beans for the image module. Registers image use cases and processing port. */
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class ImageConfigBeans {

  /** Creates the upload product image use case bean. */
  @Bean
  public UploadProductImageUseCase uploadProductImageUseCase(
      ImageProcessingPort imageProcessingPort,
      StoragePort storagePort,
      ImageRepositoryPort imageRepositoryPort,
      Logger logger) {
    return new UploadProductImageService(
        imageProcessingPort, storagePort, imageRepositoryPort, logger);
  }

  /** Creates the get product images use case bean. */
  @Bean
  public GetProductImagesUseCase getProductImagesUseCase(
      ImageRepositoryPort imageRepositoryPort, StoragePort storagePort, Logger logger) {
    return new GetProductImagesService(imageRepositoryPort, storagePort, logger);
  }

  /** Creates the delete product image use case bean. */
  @Bean
  public DeleteProductImageUseCase deleteProductImageUseCase(
      ImageRepositoryPort imageRepositoryPort, StoragePort storagePort, Logger logger) {
    return new DeleteProductImageService(imageRepositoryPort, storagePort, logger);
  }

  /** Creates the image processing port bean. */
  @Bean
  public ImageProcessingPort imageProcessingPort() {
    return new ScrimeImageProcessor();
  }
}
