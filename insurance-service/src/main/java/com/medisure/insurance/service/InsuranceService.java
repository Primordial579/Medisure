package com.medisure.insurance.service;

import com.medisure.insurance.dto.InsuranceValidityRequest;
import com.medisure.insurance.dto.InsuranceValidityResponse;
import com.medisure.insurance.entity.Insurance;
import com.medisure.insurance.repository.InsuranceRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import com.medisure.insurance.dto.ClaimDeductionRequest;
import com.medisure.insurance.dto.ClaimDeductionResponse;

@Service
public class InsuranceService {

    private static final String STATUS_VALID = "valid";
    private static final String STATUS_NOT_VALID = "not valid";
    private static final String STATUS_EXPIRED = "expired";

    private static final String REASON_NOT_FOUND = "Insurance not found";
    private static final String REASON_EXPIRED = "Insurance is expired";
    private static final String REASON_INSUFFICIENT_COVERAGE = "Estimated amount is greater than remaining coverage";
    private static final String REASON_VALID = "Insurance is active and has sufficient remaining coverage";

    private final InsuranceRepository insuranceRepository;

    public InsuranceService(InsuranceRepository insuranceRepository) {
        this.insuranceRepository = insuranceRepository;
    }

    public Insurance createInsurance(Insurance insurance) {
        if (insurance.getInsuranceId() == null || insurance.getInsuranceId().isBlank()) {
            insurance.setInsuranceId(generateInsuranceId());
        } else {
            insurance.setInsuranceId(normalizeInsuranceId(insurance.getInsuranceId()));
        }
        if (insurance.getRemainingCoverage() == null) {
            insurance.setRemainingCoverage(insurance.getCoverage());
        }
        return insuranceRepository.save(insurance);
    }

    private String generateInsuranceId() {
        long sequence = insuranceRepository.findAll().stream()
                .map(Insurance::getInsuranceId)
                .filter(existingInsuranceId -> existingInsuranceId != null && existingInsuranceId.startsWith("INS"))
                .map(existingInsuranceId -> existingInsuranceId.substring(3))
                .filter(numberPart -> !numberPart.isBlank())
                .map(numberPart -> {
                    try {
                        return Long.parseLong(numberPart);
                    } catch (NumberFormatException ex) {
                        return 1000L;
                    }
                })
                .max(Comparator.naturalOrder())
                .orElse(1000L) + 1;

        String insuranceId = "INS" + sequence;

        while (insuranceRepository.existsById(insuranceId)) {
            sequence++;
            insuranceId = "INS" + sequence;
        }

        return insuranceId;
    }

    public Optional<Insurance> getInsuranceById(String insuranceId) {
        return insuranceRepository.findById(normalizeInsuranceId(insuranceId));
    }

    public List<Insurance> getAllInsurances() {
        return insuranceRepository.findAll();
    }

    public InsuranceValidityResponse verifyInsuranceValidity(InsuranceValidityRequest request) {
        String normalizedInsuranceId = normalizeInsuranceId(request.getInsuranceId());
        Optional<Insurance> insuranceOptional = insuranceRepository.findById(normalizedInsuranceId);

        if (insuranceOptional.isEmpty()) {
            return buildValidityResponse(
                    normalizedInsuranceId,
                    request.getEstimatedAmount(),
                    null,
                    null,
                    null,
                    STATUS_NOT_VALID,
                    REASON_NOT_FOUND
            );
        }

        Insurance insurance = insuranceOptional.get();
        LocalDate today = LocalDate.now();
        BigDecimal planCoverage = resolvePlanCoverage(insurance);
        BigDecimal remainingCoverage = insurance.getRemainingCoverage() != null
                ? insurance.getRemainingCoverage()
                : planCoverage;

        if (insurance.getEndDate() != null && insurance.getEndDate().isBefore(today)) {
            return buildValidityResponse(
                    insurance.getInsuranceId(),
                    request.getEstimatedAmount(),
                    planCoverage,
                    remainingCoverage,
                    insurance.getEndDate(),
                    STATUS_EXPIRED,
                    REASON_EXPIRED
            );
        }

        boolean hasEnoughCoverage = remainingCoverage != null
                && remainingCoverage.compareTo(request.getEstimatedAmount()) >= 0;

        if (!hasEnoughCoverage) {
            return buildValidityResponse(
                    insurance.getInsuranceId(),
                    request.getEstimatedAmount(),
                    planCoverage,
                    remainingCoverage,
                    insurance.getEndDate(),
                    STATUS_NOT_VALID,
                    REASON_INSUFFICIENT_COVERAGE
            );
        }

        return buildValidityResponse(
                insurance.getInsuranceId(),
                request.getEstimatedAmount(),
                planCoverage,
                remainingCoverage,
                insurance.getEndDate(),
                STATUS_VALID,
                REASON_VALID
        );
    }

    @Transactional
    public ClaimDeductionResponse deductCoverage(String insuranceId, ClaimDeductionRequest request) {
        String normalizedInsuranceId = normalizeInsuranceId(insuranceId);

        Insurance insurance = insuranceRepository.findById(normalizedInsuranceId)
                .orElseThrow(() -> new IllegalArgumentException("Insurance not found: " + insuranceId));

        LocalDate today = LocalDate.now();
        BigDecimal planCoverage = resolvePlanCoverage(insurance);
        BigDecimal remainingCoverage = insurance.getRemainingCoverage() != null ? insurance.getRemainingCoverage() : planCoverage;

        if (insurance.getEndDate() != null && insurance.getEndDate().isBefore(today)) {
            throw new IllegalStateException("Insurance is expired");
        }

        // Cap deduction to available remaining coverage (don't reject if amount > remaining)
        BigDecimal actualDeduction = request.getAmount();
        if (remainingCoverage == null || remainingCoverage.compareTo(BigDecimal.ZERO) <= 0) {
            return new ClaimDeductionResponse(
                    request.getClaimId(),
                    normalizedInsuranceId,
                    BigDecimal.ZERO,
                    remainingCoverage != null ? remainingCoverage : BigDecimal.ZERO,
                    java.time.LocalDateTime.now(),
                    STATUS_VALID,
                    "No remaining coverage to deduct");
        }
        if (actualDeduction.compareTo(remainingCoverage) > 0) {
            actualDeduction = remainingCoverage;
        }

        // apply deduction directly on the insurance remaining coverage
        BigDecimal newRemaining = remainingCoverage.subtract(actualDeduction);
        insurance.setRemainingCoverage(newRemaining);
        insuranceRepository.save(insurance);

        return new ClaimDeductionResponse(
                request.getClaimId(),
                normalizedInsuranceId,
                actualDeduction,
                newRemaining,
                java.time.LocalDateTime.now(),
                STATUS_VALID,
                "Deduction applied");
    }

    private InsuranceValidityResponse buildValidityResponse(String insuranceId,
                                                            BigDecimal estimatedAmount,
                                                            BigDecimal planCoverage,
                                                            BigDecimal remainingCoverage,
                                                            LocalDate endDate,
                                                            String status,
                                                            String reason) {
        return new InsuranceValidityResponse(
                insuranceId,
                estimatedAmount,
                planCoverage,
                planCoverage,
                remainingCoverage,
                endDate,
                status,
                reason
        );
    }

    private BigDecimal resolvePlanCoverage(Insurance insurance) {
        if (insurance.getCoverage() != null) {
            return insurance.getCoverage();
        }
        return insurance.getLegacyCoverage();
    }

    private String normalizeInsuranceId(String insuranceId) {
        if (insuranceId == null) {
            return "";
        }
        return insuranceId.trim().toUpperCase(Locale.ROOT);
    }
}
