/* (C) 2026 */

package aros.services.rms.infraestructure.image.config;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.image.application.service.DeleteImageService;
import aros.services.rms.core.image.application.service.GetImagesService;
import aros.services.rms.core.image.application.service.UploadImageService;
import aros.services.rms.core.image.port.input.DeleteImageUseCase;
import aros.services.rms.core.image.port.input.GetImagesUseCase;
import aros.services.rms.core.image.port.input.UploadImageUseCase;
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

  /** Creates the upload image use case bean. */
  @Bean
  public UploadImageUseCase uploadImageUseCase(
      ImageProcessingPort imageProcessingPort,
      StoragePort storagePort,
      ImageRepositoryPort imageRepositoryPort,
      Logger logger) {
    return new UploadImageService(imageProcessingPort, storagePort, imageRepositoryPort, logger);
  }

  /** Creates the get images use case bean. */
  @Bean
  public GetImagesUseCase getImagesUseCase(
      ImageRepositoryPort imageRepositoryPort, StoragePort storagePort, Logger logger) {
    return new GetImagesService(imageRepositoryPort, storagePort, logger);
  }

  /** Creates the delete image use case bean. */
  @Bean
  public DeleteImageUseCase deleteImageUseCase(
      ImageRepositoryPort imageRepositoryPort, StoragePort storagePort, Logger logger) {
    return new DeleteImageService(imageRepositoryPort, storagePort, logger);
  }

  /** Creates the image processing port bean. */
  @Bean
  public ImageProcessingPort imageProcessingPort() {
    return new ScrimeImageProcessor();
  }
}
