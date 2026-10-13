package com.example.polyclinic.repository;

import com.example.polyclinic.entity.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {
    List<DoctorSchedule> findByDoctorIdAndWorkDateBetween(Long doctorId, LocalDate startDate, LocalDate endDate);
}
