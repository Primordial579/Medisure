package com.medisure.claims.dto;

import java.math.BigDecimal;

/**
 * Request body for the insurance underwriter to adjudicate (settle/deny) a claim.
 */
public class ProcessClaimRequest {

    private String decision;       // SETTLED or DENIED
    private BigDecimal approvedAmount;
    private BigDecimal deductible;
    private String remarks;

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
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

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
