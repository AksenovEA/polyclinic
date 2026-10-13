package com.example.polyclinic.controller;

import com.example.polyclinic.repository.PatientRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    private final PatientRepository patientRepository;

    public PageController(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @GetMapping("/")
    public String index()
    {
        return "index";
    }

    @GetMapping("/patients")
    public String patients(Model model)
    {
        model.addAttribute("patients", patientRepository.findAllWithDistrict());
        return "patients";
    }
}
