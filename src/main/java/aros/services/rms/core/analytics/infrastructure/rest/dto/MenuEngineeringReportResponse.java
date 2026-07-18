/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for the menu engineering BCG report — mirrors the A6 contract shape (§5.2). Contains
 * nested static types for period info, median, menu items, and cache status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Menu engineering BCG report")
public class MenuEngineeringReportResponse {

  @Schema(description = "Queried period info")
  private PeriodResponse period;

  @Schema(description = "Median volume and margin")
  private MedianResponse median;

  @Schema(description = "Per-item BCG quadrant summaries")
  private List<MenuItemResponse> items;

  @Schema(description = "Cache refresh status")
  private CacheStatusResponse cacheStatus;

  @Schema(description = "Data completeness flag: FULL | PARTIAL | EMPTY")
  private String dataCompleteness;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @Schema(description = "Human-readable notes about data quality")
  private List<String> notes;

  /** Describes the queried time range. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Queried time range")
  public static class PeriodResponse {

    @Schema(description = "Time bucket", example = "monthly")
    private String bucket;

    @Schema(description = "Inclusive start period key", example = "2026-07")
    private String from;

    @Schema(description = "Inclusive end period key", example = "2026-07")
    private String to;

    @Schema(description = "Concrete period keys included in the range")
    private List<String> keys;
  }

  /** Median volume and margin across items. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Median volume and margin across items")
  public static class MedianResponse {

    @Schema(description = "Median units sold", example = "142")
    private int volume;

    @Schema(description = "Median gross profit per unit")
    private MoneyDto margin;
  }

  /** One menu item with its BCG quadrant. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "One menu item with BCG quadrant classification")
  public static class MenuItemResponse {

    @Schema(description = "Product ID", example = "42")
    private Long productId;

    @Schema(description = "Product name", example = "Lomo en salsa")
    private String productName;

    @Schema(description = "Category ID", example = "3")
    private Long categoryId;

    @Schema(description = "Category name", example = "Platos fuertes")
    private String categoryName;

    @Schema(description = "Units sold in period", example = "320")
    private int unitsSold;

    @Schema(description = "Total revenue from this product")
    private MoneyDto revenue;

    @Schema(description = "Recipe cost for units sold")
    private MoneyDto recipeCost;

    @Schema(description = "Gross profit per unit (sell price - recipe cost)")
    private MoneyDto grossProfitPerUnit;

    @Schema(description = "Total contribution (units sold × GP per unit)")
    private MoneyDto totalContribution;

    @Schema(description = "BCG quadrant", example = "STAR")
    private String quadrant;
  }

  /** Cache refresh metadata. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Cache refresh status")
  public static class CacheStatusResponse {

    @Schema(description = "When the cache was last refreshed", example = "2026-07-17T02:00:00Z")
    private String lastRefreshedAt;

    @Schema(description = "Source data version", example = "v17")
    private String sourceVersion;

    @Schema(description = "Cache TTL in seconds", example = "86400")
    private long ttlSeconds;
  }
}
