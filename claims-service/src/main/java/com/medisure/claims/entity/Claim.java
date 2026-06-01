package com.medisure.claims.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "claims")
public class Claim extends AuditableEntity {

    @Id
    @Column(name = "claim_id", nullable = false, unique = true)
    private String claimId;

    @Column(name = "patient_id", nullable = false)
    private String patientId;

    @Column(name = "patient_name", nullable = false)
    private String patientName;

    @Column(name = "insurance_id", nullable = false)
    private String insuranceId;

    @Column(name = "diagnosis", nullable = false)
    private String diagnosis;

    @Column(name = "estimated_amount", nullable = false)
    private BigDecimal estimatedAmount;

    @Column(name = "final_bill_amount")
    private BigDecimal finalBillAmount;

    @Column(name = "preauth_status")
    private String preauthStatus;

    @Column(name = "claim_status")
    private String claimStatus;

    @Column(name = "preauthorized_amount")
    private BigDecimal preauthorizedAmount;

    @Column(name = "approved_amount")
    private BigDecimal approvedAmount;

    @Column(name = "deductible")
    private BigDecimal deductible;

    @Column(name = "payable_amount")
    private BigDecimal payableAmount;

    @Column(name = "remarks")
    private String remarks;

    // Getters and Setters

    public String getClaimId() {
        return claimId;
    }

    public void setClaimId(String claimId) {
        this.claimId = claimId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getInsuranceId() {
        return insuranceId;
    }

    public void setInsuranceId(String insuranceId) {
        this.insuranceId = insuranceId;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public BigDecimal getEstimatedAmount() {
        return estimatedAmount;
    }

    public void setEstimatedAmount(BigDecimal estimatedAmount) {
        this.estimatedAmount = estimatedAmount;
    }

    public BigDecimal getFinalBillAmount() {
        return finalBillAmount;
    }

    public void setFinalBillAmount(BigDecimal finalBillAmount) {
        this.finalBillAmount = finalBillAmount;
    }

    public String getPreauthStatus() {
        return preauthStatus;
    }

    public void setPreauthStatus(String preauthStatus) {
        this.preauthStatus = preauthStatus;
    }

    public String getClaimStatus() {
        return claimStatus;
    }

    public void setClaimStatus(String claimStatus) {
        this.claimStatus = claimStatus;
    }

    public BigDecimal getPreauthorizedAmount() {
        return preauthorizedAmount;
    }

    public void setPreauthorizedAmount(BigDecimal preauthorizedAmount) {
        this.preauthorizedAmount = preauthorizedAmount;
    }

    public BigDecimal getApprovedAmount() {
        return approvedAmount;
    }

    public void setApprovedAmount(BigDecimal approvedAmount) {
        this.approvedAmount = approvedAmount;
    }

    public BigDecimal getDeductible() {
        return deductible;
    }

    public void setDeductible(BigDecimal deductible) {
        this.deductible = deductible;
    }

    public BigDecimal getPayableAmount() {
        return payableAmount;
    }

    public void setPayableAmount(BigDecimal payableAmount) {
        this.payableAmount = payableAmount;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
