package aros.services.rms.infraestructure.image.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration properties for image storage. */
@Data
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {
  private String type = "local";
  private Local local = new Local();
  private Gcs gcs = new Gcs();

  /** Local storage configuration. */
  @Data
  public static class Local {
    private String baseDir = "./uploads";
    private String baseUrl = "http://localhost:8080/api/v1/images/local";
  }

  /** GCS storage configuration. */
  @Data
  public static class Gcs {
    private String bucket;
    private String projectId;
    private int signedUrlExpirationMinutes = 60;
  }
}
