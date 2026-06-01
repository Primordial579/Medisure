package com.medisure.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "auth_users")
public class AuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, length = 512)
    private String password;

    private String email;

    @Column(name = "phone_no")
    private String phoneNo;

    @Column(name = "user_type", nullable = false)
    private String userType;

    @Column(name = "hospital_user_id")
    private String hospitalUserId;

    @Column(name = "insurance_user_id")
    private String insuranceUserId;

    @Column(name = "created_on", updatable = false)
    private LocalDateTime createdOn;

    @Column(name = "updated_on")
    private LocalDateTime updatedOn;

    @PrePersist
    public void onPrePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdOn = now;
        this.updatedOn = now;
    }

    @PreUpdate
    public void onPreUpdate() {
        this.updatedOn = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getHospitalUserId() {
        return hospitalUserId;
    }

    public void setHospitalUserId(String hospitalUserId) {
        this.hospitalUserId = hospitalUserId;
    }

    public String getInsuranceUserId() {
        return insuranceUserId;
    }

    public void setInsuranceUserId(String insuranceUserId) {
        this.insuranceUserId = insuranceUserId;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public LocalDateTime getUpdatedOn() {
        return updatedOn;
    }
}
