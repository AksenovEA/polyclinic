package com.example.polyclinic.controller;

import com.example.polyclinic.dto.DoctorMonthScheduleResponse;
import com.example.polyclinic.entity.Appointment;
import com.example.polyclinic.entity.Doctor;
import com.example.polyclinic.entity.DoctorSchedule;
import com.example.polyclinic.repository.AppointmentRepository;
import com.example.polyclinic.repository.DoctorRepository;
import com.example.polyclinic.repository.DoctorScheduleRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/doctors")
public class DoctorSchedulePageController {

    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final AppointmentRepository appointmentRepository;

    public DoctorSchedulePageController(
            DoctorRepository doctorRepository,
            DoctorScheduleRepository doctorScheduleRepository,
            AppointmentRepository appointmentRepository
    ) {
        this.doctorRepository = doctorRepository;
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @GetMapping("/{doctorId}/schedule")
    public String schedule(
            @PathVariable Long doctorId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            Model model
    ) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Врач не найден"
                ));

        YearMonth selectedMonth = YearMonth.now();

        if (year != null && month != null) {
            try {
                selectedMonth = YearMonth.of(year, month);
            } catch (java.time.DateTimeException exception) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Некорректный год или месяц"
                );
            }
        }

        LocalDate startDate = selectedMonth.atDay(1);
        LocalDate endDate = selectedMonth.atEndOfMonth();

        List<DoctorSchedule> schedules =
                doctorScheduleRepository.findByDoctorIdAndWorkDateBetween(
                        doctorId, startDate, endDate
                );

        List<DoctorMonthScheduleResponse.DaySchedule> days =
                new ArrayList<>();

        for (DoctorSchedule schedule : schedules) {
            List<Appointment> appointments =
                    appointmentRepository.findByScheduleId(schedule.getId());

            Map<LocalTime, String> bookedSlots = new HashMap<>();

            for (Appointment appointment : appointments) {
                bookedSlots.put(
                        appointment.getAppointmentTime(),
                        appointment.getPatient().getFullName()
                );
            }

            List<DoctorMonthScheduleResponse.AppointmentSlot> slots =
                    new ArrayList<>();

            for (
                    LocalTime time = schedule.getStartTime();
                    time.isBefore(schedule.getEndTime());
                    time = time.plusMinutes(30)
            ) {
                String patientName = bookedSlots.get(time);

                slots.add(
                        new DoctorMonthScheduleResponse.AppointmentSlot(
                                time,
                                patientName == null,
                                patientName
                        )
                );
            }

            days.add(
                    new DoctorMonthScheduleResponse.DaySchedule(
                            schedule.getId(),
                            schedule.getWorkDate(),
                            schedule.getStartTime(),
                            schedule.getEndTime(),
                            slots
                    )
            );
        }

        YearMonth previousMonth = selectedMonth.minusMonths(1);
        YearMonth nextMonth = selectedMonth.plusMonths(1);

        model.addAttribute("doctor", doctor);
        model.addAttribute("days", days);
        model.addAttribute("selectedMonth", selectedMonth);
        model.addAttribute("previousMonth", previousMonth);
        model.addAttribute("nextMonth", nextMonth);

        return "doctor-schedule";
    }
}