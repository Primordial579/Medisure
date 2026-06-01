package com.medisure.insurance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InsuranceValidityResponse {

    private String insuranceId;
    private BigDecimal estimatedAmount;
    private BigDecimal coverage;
    private BigDecimal planCoverage;
    private BigDecimal remainingCoverage;
    private LocalDate endDate;
    private String status;
    private String reason;

    public InsuranceValidityResponse(String insuranceId, BigDecimal estimatedAmount, BigDecimal coverage, BigDecimal planCoverage,
                                     BigDecimal remainingCoverage, LocalDate endDate, String status, String reason) {
        this.insuranceId = insuranceId;
        this.estimatedAmount = estimatedAmount;
        this.coverage = coverage;
        this.planCoverage = planCoverage;
        this.remainingCoverage = remainingCoverage;
        this.endDate = endDate;
        this.status = status;
        this.reason = reason;
    }

    public String getInsuranceId() {
        return insuranceId;
    }

    public void setInsuranceId(String insuranceId) {
        this.insuranceId = insuranceId;
    }

    public BigDecimal getEstimatedAmount() {
        return estimatedAmount;
    }

    public void setEstimatedAmount(BigDecimal estimatedAmount) {
        this.estimatedAmount = estimatedAmount;
    }

    public BigDecimal getCoverage() {
        return coverage;
    }

    public void setCoverage(BigDecimal coverage) {
        this.coverage = coverage;
    }

    public BigDecimal getPlanCoverage() {
        return planCoverage;
    }

    public void setPlanCoverage(BigDecimal planCoverage) {
        this.planCoverage = planCoverage;
    }

    public BigDecimal getRemainingCoverage() {
        return remainingCoverage;
    }

    public void setRemainingCoverage(BigDecimal remainingCoverage) {
        this.remainingCoverage = remainingCoverage;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}