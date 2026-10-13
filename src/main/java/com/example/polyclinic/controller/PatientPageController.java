
package com.example.polyclinic.controller;

import com.example.polyclinic.entity.District;
import com.example.polyclinic.entity.Patient;
import com.example.polyclinic.repository.DistrictRepository;
import com.example.polyclinic.repository.PatientRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/patients")
public class PatientPageController {

    private final PatientRepository patientRepository;
    private final DistrictRepository districtRepository;

    public PatientPageController(
            PatientRepository patientRepository,
            DistrictRepository districtRepository
    ) {
        this.patientRepository = patientRepository;
        this.districtRepository = districtRepository;
    }

    @GetMapping("/new")
    public String newPatientForm(Model model) {
        model.addAttribute("patient", new Patient());
        model.addAttribute("districts", districtRepository.findAll());

        return "patient-form";
    }

    @PostMapping
    public String createPatient(
            Patient patient,
            Long districtId,
            RedirectAttributes redirectAttributes
    ) {
        District district = districtRepository.findById(districtId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Участок не найден")
                );

        patient.setDistrict(district);
        patientRepository.save(patient);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Пациент успешно добавлен"
        );

        return "redirect:/patients";
    }
}
