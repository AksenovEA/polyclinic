package com.example.polyclinic.controller;

import com.example.polyclinic.entity.District;
import com.example.polyclinic.repository.DistrictRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/districts")
public class DistrictController {
    private final DistrictRepository districtRepository;

    @Autowired
    public DistrictController(DistrictRepository districtRepository)
    {
        this.districtRepository = districtRepository;
    }

    @GetMapping
    public List<District> getAllDistricts()
    {
        return districtRepository.findAll();
    }

    @PostMapping
    public District ceateDistrict(@RequestBody District district)
    {
        return districtRepository.save(district);
    }
}
