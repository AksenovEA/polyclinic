package com.example.polyclinic.controller;

import com.example.polyclinic.dto.DoctorMonthScheduleResponse;
import com.example.polyclinic.entity.Appointment;
import com.example.polyclinic.entity.District;
import com.example.polyclinic.entity.Doctor;
import com.example.polyclinic.entity.DoctorSchedule;
import com.example.polyclinic.entity.util.DoctorType;
import com.example.polyclinic.repository.AppointmentRepository;
import com.example.polyclinic.repository.DistrictRepository;
import com.example.polyclinic.repository.DoctorRepository;
import com.example.polyclinic.repository.DoctorScheduleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final DistrictRepository districtRepository;

    public DoctorController(DoctorScheduleRepository doctorScheduleRepository, AppointmentRepository appointmentRepository, DoctorRepository doctorRepository, DistrictRepository districtRepository) {

        this.doctorScheduleRepository = doctorScheduleRepository;
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.districtRepository = districtRepository;
    }

    @GetMapping
    public List<Doctor> getAllDoctors()
    {
        return doctorRepository.findAll();
    }

    @PostMapping
    public Doctor createDoctor(@RequestBody Doctor doctor) {

        if (doctor.getFullName() == null
                || doctor.getFullName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ФИО врача обязательно"
            );
        }

        if (doctor.getType() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Тип врача обязателен"
            );
        }

        if (doctor.getType() == DoctorType.THERAPIST) {

            if (doctor.getDistrict() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Для терапевта необходимо указать участок"
                );
            }

            Long districtId = doctor.getDistrict().getId();

            District district = districtRepository.findById(districtId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Участок с id " + districtId + " не найден"
                    ));

            doctor.setDistrict(district);

        } else if (doctor.getType() == DoctorType.SPECIALIST) {

            if (doctor.getDistrict() != null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Специалист не должен быть прикреплён к участку"
                );
            }
        }

        return doctorRepository.save(doctor);
    }

    @GetMapping("/{doctorId}/schedule")
    public DoctorMonthScheduleResponse getMonthlySchedule(
            @PathVariable Long doctorId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        if (month < 1 || month > 12) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Месяц должен быть от 1 до 12"
            );
        }

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Врач с id " + doctorId + " не найден"
                ));

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(
                startDate.lengthOfMonth()
        );

        List<DoctorSchedule> schedules =
                doctorScheduleRepository.findByDoctorIdAndWorkDateBetween(
                        doctorId,
                        startDate,
                        endDate
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

        return new DoctorMonthScheduleResponse(
                doctor.getId(),
                doctor.getFullName(),
                year,
                month,
                days
        );
    }

}