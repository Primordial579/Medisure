package com.medisure.claims.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response from insurance-service deduct coverage API.
 */
public class CoverageDeductionResponse {

    private String claimId;
    private String insuranceId;
    private BigDecimal amount;
    private BigDecimal remainingCoverage;
    private LocalDateTime timestamp;
    private String status;
    private String reason;

    public String getClaimId() {
        return claimId;
    }

    public void setClaimId(String claimId) {
        this.claimId = claimId;
    }

    public String getInsuranceId() {
        return insuranceId;
    }

    public void setInsuranceId(String insuranceId) {
        this.insuranceId = insuranceId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getRemainingCoverage() {
        return remainingCoverage;
    }

    public void setRemainingCoverage(BigDecimal remainingCoverage) {
        this.remainingCoverage = remainingCoverage;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
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
