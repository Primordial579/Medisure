package com.medisure.insurance.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ClaimDeductionResponse {

    private String claimId;
    private String insuranceId;
    private BigDecimal amount;
    private BigDecimal remainingCoverage;
    private LocalDateTime timestamp;
    private String status;
    private String reason;

    public ClaimDeductionResponse(String claimId, String insuranceId, BigDecimal amount, BigDecimal remainingCoverage, LocalDateTime timestamp, String status, String reason) {
        this.claimId = claimId;
        this.insuranceId = insuranceId;
        this.amount = amount;
        this.remainingCoverage = remainingCoverage;
        this.timestamp = timestamp;
        this.status = status;
        this.reason = reason;
    }

    public String getClaimId() { return claimId; }
    public String getInsuranceId() { return insuranceId; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getRemainingCoverage() { return remainingCoverage; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getStatus() { return status; }
    public String getReason() { return reason; }
}
