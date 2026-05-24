package aros.services.rms.infraestructure.schedule.api.dto;

import java.util.List;

public record DayShiftsResponse(String dayOfWeek, List<ShiftDetailResponse> shifts) {}
