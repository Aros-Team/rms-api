package aros.services.rms.infraestructure.image.storage.local;

import aros.services.rms.infraestructure.image.storage.StorageProperties;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Serves local images in development profile. */
@Configuration
@Profile("development")
public class LocalResourceConfig implements WebMvcConfigurer {

  private final StorageProperties storageProperties;

  /** Creates config with storage properties. */
  public LocalResourceConfig(StorageProperties storageProperties) {
    this.storageProperties = storageProperties;
  }

  /** Registers resource handler for local image serving. */
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // Resolve relative paths to absolute paths
    String baseDir = storageProperties.getLocal().getBaseDir();
    Path absolutePath = Paths.get(baseDir).toAbsolutePath();
    String resourceLocation = "file:" + absolutePath + "/";
    
    registry
        .addResourceHandler("/api/v1/images/local/**")
        .addResourceLocations(resourceLocation);
  }
}
