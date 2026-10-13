
package com.example.polyclinic.controller;

import com.example.polyclinic.entity.Appointment;
import com.example.polyclinic.entity.DoctorSchedule;
import com.example.polyclinic.entity.Patient;
import com.example.polyclinic.repository.AppointmentRepository;
import com.example.polyclinic.repository.DoctorScheduleRepository;
import com.example.polyclinic.repository.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final DoctorScheduleRepository scheduleRepository;
    private final PatientRepository patientRepository;

    public AppointmentController(
            AppointmentRepository appointmentRepository,
            DoctorScheduleRepository scheduleRepository,
            PatientRepository patientRepository) {
        this.appointmentRepository = appointmentRepository;
        this.scheduleRepository = scheduleRepository;
        this.patientRepository = patientRepository;
    }

    @GetMapping
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @PostMapping
    public Appointment createAppointment(
            @RequestBody Appointment appointment) {

        if (appointment.getSchedule() == null
                || appointment.getSchedule().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Необходимо указать расписание врача"
            );
        }

        if (appointment.getPatient() == null
                || appointment.getPatient().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Необходимо указать пациента"
            );
        }

        if (appointment.getAppointmentTime() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Необходимо указать время приёма"
            );
        }

        Long scheduleId = appointment.getSchedule().getId();
        Long patientId = appointment.getPatient().getId();

        DoctorSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Расписание с id " + scheduleId + " не найдено"
                ));

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Пациент с id " + patientId + " не найден"
                ));

        appointment.setSchedule(schedule);
        appointment.setPatient(patient);

        return appointmentRepository.save(appointment);
    }
}
