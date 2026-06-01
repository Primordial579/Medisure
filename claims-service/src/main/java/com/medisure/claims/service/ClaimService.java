package com.medisure.claims.service;

import com.medisure.claims.dto.CoverageDeductionRequest;
import com.medisure.claims.dto.CoverageDeductionResponse;
import com.medisure.claims.dto.CreateClaimRequest;
import com.medisure.claims.dto.InsurancePolicyResponse;
import com.medisure.claims.dto.PatientResponse;
import com.medisure.claims.dto.ProcessClaimRequest;
import com.medisure.claims.dto.UpdateFinalBillRequest;
import com.medisure.claims.entity.Claim;
import com.medisure.claims.exception.ClaimNotFoundException;
import com.medisure.claims.exception.InsuranceNotFoundException;
import com.medisure.claims.exception.InvalidClaimStateException;
import com.medisure.claims.exception.PatientNotFoundException;
import com.medisure.claims.repository.ClaimRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final RestTemplate restTemplate;

    @Value("${hospital.service.base-url}")
    private String hospitalServiceBaseUrl;

    @Value("${insurance.service.base-url}")
    private String insuranceServiceBaseUrl;

    public ClaimService(ClaimRepository claimRepository, RestTemplate restTemplate) {
        this.claimRepository = claimRepository;
        this.restTemplate = restTemplate;
    }

    public Claim createClaim(CreateClaimRequest request, String userId) {

        // 1. Fetch patient details from hospital-service
        PatientResponse patient;
        try {
            String patientUrl = hospitalServiceBaseUrl + "/api/hospital/patients/" + request.getPatientId();
            ResponseEntity<PatientResponse> patientResponseEntity = restTemplate.getForEntity(patientUrl, PatientResponse.class);

            if (patientResponseEntity.getBody() == null) {
                throw new PatientNotFoundException("Patient not found with id: " + request.getPatientId());
            }
            patient = patientResponseEntity.getBody();
        } catch (HttpClientErrorException.NotFound ex) {
            throw new PatientNotFoundException("Patient not found with id: " + request.getPatientId());
        }

        if (patient.getInsuranceId() == null || patient.getInsuranceId().isBlank()) {
            throw new InsuranceNotFoundException("No insurance linked to patient: " + request.getPatientId());
        }

        // 2. Fetch insurance details from insurance-service using the insuranceId from patient
        InsurancePolicyResponse insurance;
        try {
            String insuranceUrl = insuranceServiceBaseUrl + "/api/insurance/insurances/" + patient.getInsuranceId();
            ResponseEntity<InsurancePolicyResponse> insuranceResponseEntity = restTemplate.getForEntity(insuranceUrl, InsurancePolicyResponse.class);

            if (insuranceResponseEntity.getBody() == null) {
                throw new InsuranceNotFoundException("Insurance not found with id: " + patient.getInsuranceId());
            }
            insurance = insuranceResponseEntity.getBody();
        } catch (HttpClientErrorException.NotFound ex) {
            throw new InsuranceNotFoundException("Insurance not found with id: " + patient.getInsuranceId());
        }

        // 3. Build the claim
        Claim claim = new Claim();
        claim.setClaimId(generateClaimId());
        claim.setPatientId(patient.getPatientId());
        claim.setPatientName(patient.getName());
        claim.setInsuranceId(insurance.getInsuranceId());
        claim.setDiagnosis(request.getDiagnosis());
        claim.setEstimatedAmount(request.getEstimatedAmount());
        claim.setCreatedBy(userId);
        claim.setUpdatedBy(userId);

        // 4. Automated pre-authorization decision
        performPreAuthorization(claim, insurance);

        // 5. If pre-auth approved, deduct pre-authorized amount from insurance coverage
        if ("PREAUTH_APPROVED".equals(claim.getClaimStatus())) {
            deductCoverage(claim.getInsuranceId(), claim.getClaimId(), claim.getPreauthorizedAmount());
        }

        return claimRepository.save(claim);
    }

    public List<Claim> getAllClaims() {
        return claimRepository.findAll();
    }

    public Claim getClaimById(String claimId) {
        return claimRepository.findById(claimId)
                .orElseThrow(() -> new ClaimNotFoundException("Claim not found with id: " + claimId));
    }

    public Claim updateFinalBill(String claimId, UpdateFinalBillRequest request, String userId) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ClaimNotFoundException("Claim not found with id: " + claimId));

        if (!"PREAUTH_APPROVED".equals(claim.getClaimStatus()) && !"PREAUTH_REJECTED".equals(claim.getClaimStatus())) {
            throw new InvalidClaimStateException(
                    "Final bill can only be uploaded for pre-authorized or pre-auth rejected claims. Current status: " + claim.getClaimStatus());
        }

        if (request.getFinalBillAmount() == null || request.getFinalBillAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Final bill amount must be greater than zero");
        }

        claim.setFinalBillAmount(request.getFinalBillAmount());
        claim.setUpdatedBy(userId);

        // If pre-auth was rejected, move directly to UNDER_REVIEW for underwriter adjudication
        if ("PREAUTH_REJECTED".equals(claim.getClaimStatus())) {
            claim.setClaimStatus("UNDER_REVIEW");
            claim.setRemarks("Final bill (" + request.getFinalBillAmount()
                    + ") uploaded. Pre-auth was rejected, pending underwriter adjudication.");
            return claimRepository.save(claim);
        }

        // Calculate adjustment: difference between final bill and already-deducted preauth amount
        BigDecimal preauthorized = claim.getPreauthorizedAmount() != null ? claim.getPreauthorizedAmount() : BigDecimal.ZERO;
        BigDecimal adjustment = request.getFinalBillAmount().subtract(preauthorized);

        if (adjustment.compareTo(BigDecimal.ZERO) <= 0) {
            // Final bill is within or below pre-authorized amount → auto-settle
            claim.setApprovedAmount(request.getFinalBillAmount());
            claim.setDeductible(BigDecimal.ZERO);
            claim.setPayableAmount(request.getFinalBillAmount());
            claim.setClaimStatus("SETTLED");
            claim.setRemarks("Auto-settled. Final bill (" + request.getFinalBillAmount()
                    + ") is within the pre-authorized amount (" + preauthorized
                    + "). No additional deduction required.");
        } else {
            // Final bill exceeds pre-authorized amount → needs underwriter review for the difference
            claim.setClaimStatus("UNDER_REVIEW");
            claim.setRemarks("Final bill (" + request.getFinalBillAmount()
                    + ") exceeds pre-authorized amount (" + preauthorized
                    + "). Additional amount of " + adjustment + " pending underwriter adjudication.");
        }

        return claimRepository.save(claim);
    }

    /**
     * Insurance underwriter adjudicates the claim after final bill is uploaded.
     * Decision: SETTLED (approved) or DENIED (rejected).
     */
    public Claim processClaim(String claimId, ProcessClaimRequest request, String userId) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ClaimNotFoundException("Claim not found with id: " + claimId));

        if (!"UNDER_REVIEW".equals(claim.getClaimStatus()) && !"PREAUTH_REJECTED".equals(claim.getClaimStatus())) {
            throw new InvalidClaimStateException(
                    "Claim can only be processed when it is UNDER_REVIEW or PREAUTH_REJECTED. Current status: " + claim.getClaimStatus());
        }

        String decision = request.getDecision() != null ? request.getDecision().toUpperCase() : "";

        boolean preauthWasDeducted = "APPROVED".equals(claim.getPreauthStatus());
        BigDecimal alreadyDeducted = preauthWasDeducted
                ? (claim.getPreauthorizedAmount() != null ? claim.getPreauthorizedAmount() : BigDecimal.ZERO)
                : BigDecimal.ZERO;
        claim.setUpdatedBy(userId);

        if ("SETTLED".equals(decision)) {
            BigDecimal approved = request.getApprovedAmount() != null ? request.getApprovedAmount() : BigDecimal.ZERO;
            BigDecimal deductible = request.getDeductible() != null ? request.getDeductible() : BigDecimal.ZERO;
            BigDecimal finalBill = claim.getFinalBillAmount() != null ? claim.getFinalBillAmount() : claim.getEstimatedAmount();

            BigDecimal amountToDeduct;
            BigDecimal payable;
            String remarkText;

            if (preauthWasDeducted) {
                // Pre-auth was approved and already deducted from coverage.
                // approved = TOTAL insurance coverage (inclusive of preauth).
                // Only deduct the difference.
                amountToDeduct = approved.subtract(alreadyDeducted);
                if (amountToDeduct.compareTo(BigDecimal.ZERO) < 0) {
                    amountToDeduct = BigDecimal.ZERO;
                }
                payable = finalBill.subtract(approved);
                remarkText = "Claim settled. Total insurance covers: " + approved
                        + " (preauth already deducted: " + alreadyDeducted
                        + ", additional deduction: " + amountToDeduct
                        + "). Patient pays: ";
            } else {
                // Pre-auth was REJECTED — nothing was deducted before.
                // approved = amount underwriter approves, deducted directly from remaining coverage.
                amountToDeduct = approved;
                payable = finalBill.subtract(approved);
                remarkText = "Claim settled. Insurance covers: " + approved
                        + " (deducted from remaining coverage). Patient pays: ";
            }

            if (payable.compareTo(BigDecimal.ZERO) < 0) {
                payable = BigDecimal.ZERO;
            }

            claim.setApprovedAmount(approved);
            claim.setDeductible(deductible);
            claim.setPayableAmount(payable);
            claim.setClaimStatus("SETTLED");
            claim.setRemarks(request.getRemarks() != null ? request.getRemarks()
                    : remarkText + payable);

            if (amountToDeduct.compareTo(BigDecimal.ZERO) > 0) {
                deductCoverage(claim.getInsuranceId(), claim.getClaimId(), amountToDeduct);
            }

        } else if ("DENIED".equals(decision)) {
            claim.setApprovedAmount(BigDecimal.ZERO);
            claim.setDeductible(BigDecimal.ZERO);
            claim.setPayableAmount(BigDecimal.ZERO);
            claim.setClaimStatus("DENIED");
            claim.setRemarks(request.getRemarks() != null ? request.getRemarks()
                    : "Claim denied by underwriter.");

        } else {
            throw new IllegalArgumentException("Invalid decision. Must be SETTLED or DENIED.");
        }

        return claimRepository.save(claim);
    }

    private String generateClaimId() {
        long sequence = claimRepository.findAll().stream()
                .map(Claim::getClaimId)
                .filter(id -> id != null && id.startsWith("CLM"))
                .map(id -> id.substring(3))
                .filter(num -> !num.isBlank())
                .map(num -> {
                    try {
                        return Long.parseLong(num);
                    } catch (NumberFormatException e) {
                        return 1000L;
                    }
                })
                .max(Comparator.naturalOrder())
                .orElse(1000L) + 1;

        String claimId = "CLM" + sequence;

        while (claimRepository.existsById(claimId)) {
            sequence++;
            claimId = "CLM" + sequence;
        }

        return claimId;
    }

    /**
     * Automated pre-authorization based on estimated amount and preauth percentage.
     * preauthorized_amount = estimated_amount * (preauth_percentage / 100)
     * If estimated_amount > remaining_coverage → REJECTED (claim exceeds coverage).
     * If preauthorized_amount <= remaining_coverage → APPROVED, else → REJECTED.
     */
    private void performPreAuthorization(Claim claim, InsurancePolicyResponse insurance) {
        BigDecimal remainingCoverage = insurance.getRemainingCoverage();
        BigDecimal preauthPct = insurance.getPreauthPercentage();

        if (remainingCoverage == null || preauthPct == null) {
            claim.setPreauthStatus("REJECTED");
            claim.setClaimStatus("PREAUTH_REJECTED");
            claim.setRemarks("Pre-authorization rejected: Insurance coverage or pre-auth percentage data is unavailable.");
            return;
        }

        // If remaining coverage is zero or negative, deny the claim immediately
        if (remainingCoverage.compareTo(BigDecimal.ZERO) <= 0) {
            claim.setPreauthStatus("REJECTED");
            claim.setClaimStatus("DENIED");
            claim.setApprovedAmount(BigDecimal.ZERO);
            claim.setPayableAmount(BigDecimal.ZERO);
            claim.setDeductible(BigDecimal.ZERO);
            claim.setRemarks("Claim denied. Insurance coverage is fully exhausted (remaining coverage: "
                    + remainingCoverage + "). No further claims can be processed.");
            return;
        }

        // First check: if estimated amount itself exceeds remaining coverage, reject pre-auth
        if (claim.getEstimatedAmount().compareTo(remainingCoverage) > 0) {
            BigDecimal preauthorizedAmount = claim.getEstimatedAmount()
                    .multiply(preauthPct)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            claim.setPreauthorizedAmount(preauthorizedAmount);
            claim.setPreauthStatus("REJECTED");
            claim.setClaimStatus("PREAUTH_REJECTED");
            claim.setRemarks("Pre-authorization rejected. Estimated amount " + claim.getEstimatedAmount()
                    + " exceeds remaining coverage of " + remainingCoverage + ".");
            return;
        }

        // Calculate the pre-authorized amount as a percentage of the estimated amount
        BigDecimal preauthorizedAmount = claim.getEstimatedAmount()
                .multiply(preauthPct)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        claim.setPreauthorizedAmount(preauthorizedAmount);

        if (preauthorizedAmount.compareTo(remainingCoverage) <= 0) {
            claim.setPreauthStatus("APPROVED");
            claim.setClaimStatus("PREAUTH_APPROVED");
            claim.setRemarks("Pre-authorization approved. Pre-authorized amount " + preauthorizedAmount
                    + " (" + preauthPct + "% of estimated " + claim.getEstimatedAmount()
                    + ") is within remaining coverage of " + remainingCoverage + ".");
        } else {
            claim.setPreauthStatus("REJECTED");
            claim.setClaimStatus("PREAUTH_REJECTED");
            claim.setRemarks("Pre-authorization rejected. Pre-authorized amount " + preauthorizedAmount
                    + " (" + preauthPct + "% of estimated " + claim.getEstimatedAmount()
                    + ") exceeds remaining coverage of " + remainingCoverage + ".");
        }
    }

    /**
     * Calls insurance-service to deduct the given amount from the policy's remaining coverage.
     */
    private void deductCoverage(String insuranceId, String claimId, BigDecimal amount) {
        String deductUrl = insuranceServiceBaseUrl + "/api/insurance/insurances/" + insuranceId + "/deduct";
        CoverageDeductionRequest deductionRequest = new CoverageDeductionRequest(claimId, amount);
        try {
            restTemplate.postForEntity(deductUrl, deductionRequest, CoverageDeductionResponse.class);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to deduct coverage from insurance " + insuranceId + ": " + ex.getMessage(), ex);
        }
    }
}
