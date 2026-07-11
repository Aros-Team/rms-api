package aros.services.rms.infraestructure.schedule.api.dto;

/** Response with a single shift detail for the worker's schedule. */
public record ShiftDetailResponse(String scheduleName, String startTime, String endTime) {}
