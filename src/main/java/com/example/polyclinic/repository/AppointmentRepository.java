package com.example.polyclinic.repository;

import com.example.polyclinic.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByScheduleId(Long scheduleId);
    boolean existsByScheduleIdAndAppointmentTime(Long scheduleId, LocalTime appointmentTime);
}
