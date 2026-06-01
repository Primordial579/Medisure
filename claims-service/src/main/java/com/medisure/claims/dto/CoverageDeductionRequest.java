package com.medisure.claims.dto;

import java.math.BigDecimal;

/**
 * Request to deduct coverage from insurance-service.
 * Maps to POST /api/insurance/insurances/{insuranceId}/deduct
 */
public class CoverageDeductionRequest {

    private String claimId;
    private BigDecimal amount;

    public CoverageDeductionRequest() {}

    public CoverageDeductionRequest(String claimId, BigDecimal amount) {
        this.claimId = claimId;
        this.amount = amount;
    }

    public String getClaimId() {
        return claimId;
    }

    public void setClaimId(String claimId) {
        this.claimId = claimId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
