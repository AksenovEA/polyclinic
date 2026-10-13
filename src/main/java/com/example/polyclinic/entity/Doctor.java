package com.example.polyclinic.entity;

import com.example.polyclinic.entity.util.DoctorType;
import jakarta.persistence.*;

@Entity
@Table(name = "doctor")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private DoctorType type;

    @ManyToOne
    @JoinColumn(name = "district_id")
    private District district;

    public Doctor() {
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setType(DoctorType type) {
        this.type = type;
    }

    public void setDistrict(District district) {
        this.district = district;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public DoctorType getType() {
        return type;
    }

    public District getDistrict() {
        return district;
    }
}
