
package com.example.polyclinic.controller;

import com.example.polyclinic.entity.Doctor;
import com.example.polyclinic.entity.DoctorSchedule;
import com.example.polyclinic.repository.DoctorRepository;
import com.example.polyclinic.repository.DoctorScheduleRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;

@Controller
@RequestMapping("/schedules")
public class SchedulePageController {

    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository scheduleRepository;

    public SchedulePageController(
            DoctorRepository doctorRepository,
            DoctorScheduleRepository scheduleRepository
    ) {
        this.doctorRepository = doctorRepository;
        this.scheduleRepository = scheduleRepository;
    }

    @GetMapping("/new")
    public String newScheduleForm(Model model) {
        model.addAttribute("doctors", doctorRepository.findAll());
        return "schedule-form";
    }

    @PostMapping
    public String createSchedule(
            Long doctorId,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate workDate,
            String shift,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (doctorId == null || workDate == null
                || shift == null || shift.isBlank()) {
            model.addAttribute("errorMessage",
                    "Выберите врача, дату и смену");
            model.addAttribute("doctors", doctorRepository.findAll());
            return "schedule-form";
        }

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElse(null);

        if (doctor == null) {
            model.addAttribute("errorMessage", "Врач не найден");
            model.addAttribute("doctors", doctorRepository.findAll());
            return "schedule-form";
        }

        LocalTime startTime;
        LocalTime endTime;

        if ("MORNING".equals(shift)) {
            startTime = LocalTime.of(8, 0);
            endTime = LocalTime.of(12, 0);
        } else if ("AFTERNOON".equals(shift)) {
            startTime = LocalTime.of(13, 0);
            endTime = LocalTime.of(17, 0);
        } else {
            model.addAttribute("errorMessage", "Неизвестная смена");
            model.addAttribute("doctors", doctorRepository.findAll());
            return "schedule-form";
        }

        DoctorSchedule schedule = new DoctorSchedule();
        schedule.setDoctor(doctor);
        schedule.setWorkDate(workDate);
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);

        try {
            scheduleRepository.saveAndFlush(schedule);
        } catch (DataIntegrityViolationException exception) {
            model.addAttribute("errorMessage",
                    "Не удалось создать расписание. "
                            + "Проверьте рабочий день, смену и ограничения расписания.");
            model.addAttribute("doctors", doctorRepository.findAll());
            return "schedule-form";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage", "Расписание успешно создано"
        );

        return "redirect:/doctors/" + doctorId + "/schedule?year="
                + workDate.getYear() + "&month=" + workDate.getMonthValue();
    }
}
