
package com.example.polyclinic.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record DoctorMonthScheduleResponse(
        Long doctorId,
        String doctorName,
        int year,
        int month,
        List<DaySchedule> days
) {
    public record DaySchedule(
            Long scheduleId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            List<AppointmentSlot> slots
    ) {
    }

    public record AppointmentSlot(
            LocalTime time,
            boolean available,
            String patientName
    ) {
    }
}
