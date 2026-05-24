package aros.services.rms.infraestructure.schedule.api.dto;

import java.util.List;

/** Response with the shifts for a given day. */
public record DayShiftsResponse(String dayOfWeek, List<ShiftDetailResponse> shifts) {}
