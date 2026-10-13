package com.example.polyclinic.repository;


import com.example.polyclinic.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    @Query("SELECT p FROM Patient p JOIN FETCH p.district")
    List<Patient> findAllWithDistrict();
}
