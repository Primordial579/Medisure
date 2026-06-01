package com.medisure.insurance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "insurances")
public class Insurance extends AuditableEntity {

    @Id
    @Column(name = "insurance_id", unique = true, nullable = false)
    private String insuranceId;

    private String name;
    private Integer age;
    private LocalDate dob;

    @Column(name = "phone_no")
    private String phoneNo;

    @Column(name = "mail_id")
    private String mailId;

    @Column(name = "blood_group")
    private String bloodGroup;

    @Column(name = "plan_coverage")
    private BigDecimal coverage;

    @Column(name = "coverage", insertable = false, updatable = false)
    private BigDecimal legacyCoverage;

    @Column(name = "remaining_coverage")
    private BigDecimal remainingCoverage;

    @Column(name = "preauth_percentage")
    private BigDecimal preauthPercentage;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "policy_type")
    private String policyType;

    @Column(name = "created_by")
    private String createdBy;

    public String getInsuranceId() {
        return insuranceId;
    }

    public void setInsuranceId(String insuranceId) {
        this.insuranceId = insuranceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getMailId() {
        return mailId;
    }

    public void setMailId(String mailId) {
        this.mailId = mailId;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public BigDecimal getCoverage() {
        return coverage;
    }

    public void setCoverage(BigDecimal coverage) {
        this.coverage = coverage;
    }

    public BigDecimal getPreauthPercentage() {
        return preauthPercentage;
    }

    public void setPreauthPercentage(BigDecimal preauthPercentage) {
        this.preauthPercentage = preauthPercentage;
    }

    public BigDecimal getRemainingCoverage() {
        return remainingCoverage;
    }

    public void setRemainingCoverage(BigDecimal remainingCoverage) {
        this.remainingCoverage = remainingCoverage;
    }

    public BigDecimal getLegacyCoverage() {
        return legacyCoverage;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getPolicyType() {
        return policyType;
    }

    public void setPolicyType(String policyType) {
        this.policyType = policyType;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
