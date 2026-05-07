package aros.services.rms.core.image.domain;

/** Image size variants for responsive display. */
public enum ImageSize {
  MOBILE(400, 75),
  TABLET(800, 80),
  DESKTOP(1200, 85);

  public final int maxWidth;
  public final int quality;

  ImageSize(int maxWidth, int quality) {
    this.maxWidth = maxWidth;
    this.quality = quality;
  }
}
