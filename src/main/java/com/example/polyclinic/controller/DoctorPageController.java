
package com.example.polyclinic.controller;

import com.example.polyclinic.entity.District;
import com.example.polyclinic.entity.Doctor;
import com.example.polyclinic.entity.util.DoctorType;
import com.example.polyclinic.repository.DistrictRepository;
import com.example.polyclinic.repository.DoctorRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/doctors")
public class DoctorPageController {

    private final DoctorRepository doctorRepository;
    private final DistrictRepository districtRepository;

    public DoctorPageController(
            DoctorRepository doctorRepository,
            DistrictRepository districtRepository
    ) {
        this.doctorRepository = doctorRepository;
        this.districtRepository = districtRepository;
    }

    @GetMapping
    public String doctors(Model model) {
        model.addAttribute("doctors", doctorRepository.findAll());
        return "doctors";
    }

    @GetMapping("/new")
    public String newDoctorForm(Model model) {
        model.addAttribute("doctor", new Doctor());
        model.addAttribute("districts", districtRepository.findAll());
        return "doctor-form";
    }

    @PostMapping
    public String createDoctor(
            String fullName,
            DoctorType type,
            Long districtId,
            RedirectAttributes redirectAttributes
    ) {
        if (fullName == null || fullName.isBlank()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Укажите ФИО врача"
            );
            return "redirect:/doctors/new";
        }

        if (type == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Выберите тип врача"
            );
            return "redirect:/doctors/new";
        }

        Doctor doctor = new Doctor();
        doctor.setFullName(fullName.trim());
        doctor.setType(type);

        if (type == DoctorType.THERAPIST) {
            if (districtId == null) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "Для терапевта необходимо выбрать участок"
                );
                return "redirect:/doctors/new";
            }

            District district = districtRepository.findById(districtId)
                    .orElse(null);

            if (district == null) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage", "Выбранный участок не найден"
                );
                return "redirect:/doctors/new";
            }

            doctor.setDistrict(district);
        } else {
            // Специалисты не прикрепляются к участкам.
            doctor.setDistrict(null);
        }

        doctorRepository.save(doctor);

        redirectAttributes.addFlashAttribute(
                "successMessage", "Врач успешно добавлен"
        );

        return "redirect:/doctors";
    }
}
