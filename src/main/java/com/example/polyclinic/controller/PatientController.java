package com.example.polyclinic.controller;

import com.example.polyclinic.entity.District;
import com.example.polyclinic.entity.Patient;
import com.example.polyclinic.repository.DistrictRepository;
import com.example.polyclinic.repository.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {
    private final PatientRepository patientRepository;
    private final DistrictRepository districtRepository;

    public PatientController(PatientRepository patientRepository, DistrictRepository districtRepository) {
        this.patientRepository = patientRepository;
        this.districtRepository = districtRepository;
    }

    @GetMapping
    public List<Patient> getAllPatients()
    {
        return patientRepository.findAll();
    }

    @PostMapping
    public Patient createPatient(@RequestBody Patient patient)
    {
        Long districtId = patient.getDistrict().getId();
        District district = districtRepository.findById(districtId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Участок с id " + districtId + " не найден"
                ));
        patient.setDistrict(district);
        return patientRepository.save(patient);
    }
}
