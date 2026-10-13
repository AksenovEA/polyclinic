
package com.example.polyclinic.controller;

import com.example.polyclinic.entity.Appointment;
import com.example.polyclinic.entity.DoctorSchedule;
import com.example.polyclinic.entity.Patient;
import com.example.polyclinic.repository.AppointmentRepository;
import com.example.polyclinic.repository.DoctorScheduleRepository;
import com.example.polyclinic.repository.PatientRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalTime;

@Controller
@RequestMapping("/appointments")
public class AppointmentPageController {

    private final AppointmentRepository appointmentRepository;
    private final DoctorScheduleRepository scheduleRepository;
    private final PatientRepository patientRepository;

    public AppointmentPageController(
            AppointmentRepository appointmentRepository,
            DoctorScheduleRepository scheduleRepository,
            PatientRepository patientRepository) {
        this.appointmentRepository = appointmentRepository;
        this.scheduleRepository = scheduleRepository;
        this.patientRepository = patientRepository;
    }

    @GetMapping("/new")
    public String newAppointmentForm(
            @RequestParam Long scheduleId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime time,
            Model model) {

        DoctorSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Расписание не найдено"));

        if (time == null
                || time.isBefore(schedule.getStartTime())
                || !time.isBefore(schedule.getEndTime())
                || time.getMinute() % 30 != 0
                || time.getSecond() != 0
                || time.getNano() != 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Некорректное время приёма");
        }

        if (appointmentRepository
                .existsByScheduleIdAndAppointmentTime(scheduleId, time)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Это время уже занято");
        }

        model.addAttribute("schedule", schedule);
        model.addAttribute("time", time);
        model.addAttribute("patients", patientRepository.findAllWithDistrict());

        return "appointment-form";
    }

    @PostMapping
    public String createAppointment(
            @RequestParam Long scheduleId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime time,
            @RequestParam Long patientId,
            Model model,
            RedirectAttributes redirectAttributes) {

        DoctorSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Расписание не найдено"));

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Пациент не найден"));

        if (time == null
                || time.isBefore(schedule.getStartTime())
                || !time.isBefore(schedule.getEndTime())
                || time.getMinute() % 30 != 0
                || time.getSecond() != 0
                || time.getNano() != 0) {
            model.addAttribute("errorMessage", "Некорректное время приёма");
            return fillForm(model, schedule, time);
        }

        if (appointmentRepository
                .existsByScheduleIdAndAppointmentTime(scheduleId, time)) {
            model.addAttribute("errorMessage",
                    "Это время уже занято. Выберите другой свободный слот.");
            return fillForm(model, schedule, time);
        }

        Appointment appointment = new Appointment();
        appointment.setSchedule(schedule);
        appointment.setPatient(patient);
        appointment.setAppointmentTime(time);

        try {
            appointmentRepository.saveAndFlush(appointment);
        } catch (DataIntegrityViolationException exception) {
            model.addAttribute("errorMessage",
                    "Не удалось записать пациента. Возможно, слот уже занят "
                            + "или запись нарушает правила расписания.");
            return fillForm(model, schedule, time);
        }

        redirectAttributes.addFlashAttribute(
                "successMessage", "Пациент успешно записан на приём");

        return "redirect:/doctors/" + schedule.getDoctor().getId()
                + "/schedule?year=" + schedule.getWorkDate().getYear()
                + "&month=" + schedule.getWorkDate().getMonthValue();
    }

    private String fillForm(
            Model model, DoctorSchedule schedule, LocalTime time) {
        model.addAttribute("schedule", schedule);
        model.addAttribute("time", time);
        model.addAttribute("patients", patientRepository.findAllWithDistrict());
        return "appointment-form";
    }
}
