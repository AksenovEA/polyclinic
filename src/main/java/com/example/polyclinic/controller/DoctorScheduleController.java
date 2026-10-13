package com.example.polyclinic.controller;

import com.example.polyclinic.entity.Doctor;
import com.example.polyclinic.entity.DoctorSchedule;
import com.example.polyclinic.repository.DoctorRepository;
import com.example.polyclinic.repository.DoctorScheduleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/doctor-schedules")
public class DoctorScheduleController {

    private final DoctorScheduleRepository doctorScheduleRepository;
    private final DoctorRepository doctorRepository;

    public DoctorScheduleController(DoctorScheduleRepository doctorScheduleRepository, DoctorRepository doctorRepository) {
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.doctorRepository = doctorRepository;
    }

    @GetMapping
    public List<DoctorSchedule> getAllDoctorSchedules()
    {
        return doctorScheduleRepository.findAll();
    }

    @PostMapping
    public DoctorSchedule createDoctorSchedule(@RequestBody DoctorSchedule doctorSchedule)
    {
        if (doctorSchedule.getDoctor() == null || doctorSchedule.getDoctor().getId() == null)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "необходимо указать врача");
        }
        Long doctorId = doctorSchedule.getDoctor().getId();

        Doctor doctor = doctorRepository.findById(doctorId).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "врач с id " + doctorId + " не найден"));

        if (doctorSchedule.getWorkDate() == null || doctorSchedule.getStartTime() == null || doctorSchedule.getEndTime() == null)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "необходимо указать дату и время работы");

        }

        doctorSchedule.setDoctor(doctor);
        return doctorScheduleRepository.save(doctorSchedule);
    }
}
