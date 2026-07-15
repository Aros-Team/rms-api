package aros.services.rms.infraestructure.specialselection.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Envelope DTO wrapping a special selection change event pushed via WebSocket. */
@Schema(description = "Special selection update event")
public record SpecialSelectionUpdateEvent(
    @Schema(description = "Type of change", example = "UPDATE") String changeType,
    @Schema(description = "Product ID affected", example = "7") Long productId,
    @Schema(description = "Whether the combo is active after this event", example = "true")
        boolean active,
    @Schema(description = "Full updated configuration (null when changeType=DELETE)")
        SpecialSelectionResponse selection) {

  /**
   * Builds an event envelope from a change type and the optional selection payload.
   *
   * @param changeType the type of change as a string
   * @param selection the updated selection (null for DELETE events)
   * @return the event envelope
   */
  public static SpecialSelectionUpdateEvent of(
      String changeType, SpecialSelectionResponse selection) {
    boolean active = selection != null && selection.active();
    return new SpecialSelectionUpdateEvent(
        changeType, selection != null ? selection.productId() : null, active, selection);
  }
}
