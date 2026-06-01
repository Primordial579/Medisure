package com.medisure.insurance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class InsuranceValidityRequest {

    @NotBlank(message = "insuranceId is required")
    private String insuranceId;

    @NotNull(message = "estimatedAmount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "estimatedAmount must be greater than 0")
    private BigDecimal estimatedAmount;

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
}