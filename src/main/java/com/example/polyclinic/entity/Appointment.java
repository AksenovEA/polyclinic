package com.example.polyclinic.entity;

import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
@Table(name = "appointment")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private DoctorSchedule schedule;

    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @Column(name = "appointment_time", nullable = false)
    private LocalTime appointmentTime;

    public Appointment() {
    }

    public Long getId() {
        return id;
    }

    public DoctorSchedule getSchedule() {
        return schedule;
    }

    public Patient getPatient() {
        return patient;
    }

    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setSchedule(DoctorSchedule schedule) {
        this.schedule = schedule;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public void setAppointmentTime(LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }
}
