package com.medisure.claims.dto;

import java.math.BigDecimal;

public class UpdateFinalBillRequest {

    private BigDecimal finalBillAmount;

    public BigDecimal getFinalBillAmount() {
        return finalBillAmount;
    }

    public void setFinalBillAmount(BigDecimal finalBillAmount) {
        this.finalBillAmount = finalBillAmount;
    }
}
