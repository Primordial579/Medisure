package com.medisure.auth.dto;

public class RegisterRequest {

    private String username;
    private String password;
    private String email;
    private String phoneNo;
    private String userType;
    private String hospitalUserId;
    private String insuranceUserId;

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
}
