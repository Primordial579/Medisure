package com.medisure.claims.service;

import com.medisure.claims.dto.*;
import com.medisure.claims.entity.Claim;
import com.medisure.claims.exception.ClaimNotFoundException;
import com.medisure.claims.exception.InsuranceNotFoundException;
import com.medisure.claims.exception.InvalidClaimStateException;
import com.medisure.claims.exception.PatientNotFoundException;
import com.medisure.claims.repository.ClaimRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ClaimService claimService;

    private CreateClaimRequest createClaimRequest;
    private PatientResponse patientResponse;
    private InsurancePolicyResponse insuranceResponse;

    @BeforeEach
    void setUp() {
        createClaimRequest = new CreateClaimRequest();
        createClaimRequest.setPatientId("PA1001");
        createClaimRequest.setDiagnosis("Fever");
        createClaimRequest.setEstimatedAmount(BigDecimal.valueOf(50000));

        patientResponse = new PatientResponse();
        patientResponse.setPatientId("PA1001");
        patientResponse.setName("John Doe");
        patientResponse.setInsuranceId("INS1001");

        insuranceResponse = new InsurancePolicyResponse();
        insuranceResponse.setInsuranceId("INS1001");
        insuranceResponse.setName("John Doe");
        insuranceResponse.setCoverage(BigDecimal.valueOf(500000));
        insuranceResponse.setRemainingCoverage(BigDecimal.valueOf(500000));
    }

    @Test
    void createClaim_patientNotFound_throwsPatientNotFoundException() {
        when(restTemplate.getForEntity(anyString(), eq(PatientResponse.class)))
                .thenThrow(new org.springframework.web.client.HttpClientErrorException(
                        org.springframework.http.HttpStatus.NOT_FOUND));

        assertThrows(PatientNotFoundException.class,
                () -> claimService.createClaim(createClaimRequest, "HU1001"));
    }

    @Test
    void createClaim_patientHasNoInsurance_throwsInsuranceNotFoundException() {
        patientResponse.setInsuranceId(null);

        when(restTemplate.getForEntity(
                contains("/api/hospital/patients/PA1001"),
                eq(PatientResponse.class)))
                .thenReturn(new org.springframework.http.ResponseEntity<>(patientResponse, org.springframework.http.HttpStatus.OK));

        assertThrows(InsuranceNotFoundException.class,
                () -> claimService.createClaim(createClaimRequest, "HU1001"));
    }

    @Test
    void createClaim_insuranceNotFound_throwsInsuranceNotFoundException() {
        when(restTemplate.getForEntity(
                contains("/api/hospital/patients/PA1001"),
                eq(PatientResponse.class)))
                .thenReturn(new org.springframework.http.ResponseEntity<>(patientResponse, org.springframework.http.HttpStatus.OK));

        when(restTemplate.getForEntity(
                contains("/api/insurance/insurances/INS1001"),
                eq(InsurancePolicyResponse.class)))
                .thenThrow(new org.springframework.web.client.HttpClientErrorException(
                        org.springframework.http.HttpStatus.NOT_FOUND));

        assertThrows(InsuranceNotFoundException.class,
                () -> claimService.createClaim(createClaimRequest, "HU1001"));
    }

    @Test
    void createClaim_preauthApproved_savesClaimWithApprovedStatus() {
        when(restTemplate.getForEntity(
                contains("/api/hospital/patients/PA1001"),
                eq(PatientResponse.class)))
                .thenReturn(new org.springframework.http.ResponseEntity<>(patientResponse, org.springframework.http.HttpStatus.OK));

        when(restTemplate.getForEntity(
                contains("/api/insurance/insurances/INS1001"),
                eq(InsurancePolicyResponse.class)))
                .thenReturn(new org.springframework.http.ResponseEntity<>(insuranceResponse, org.springframework.http.HttpStatus.OK));

        when(claimRepository.findAll()).thenReturn(new ArrayList<>());
        when(claimRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(restTemplate.postForEntity(anyString(), any(), any())).thenReturn(
                new org.springframework.http.ResponseEntity<>(new CoverageDeductionResponse(), org.springframework.http.HttpStatus.OK));

        Claim result = claimService.createClaim(createClaimRequest, "HU1001");

        assertNotNull(result);
        assertNotNull(result.getClaimId());
        assertTrue(result.getClaimId().startsWith("CLM"));
    }

    @Test
    void getAllClaims_returnsList() {
        List<Claim> claims = new ArrayList<>();
        claims.add(new Claim());
        when(claimRepository.findAll()).thenReturn(claims);

        List<Claim> result = claimService.getAllClaims();

        assertEquals(1, result.size());
    }

    @Test
    void getClaimById_found() {
        Claim claim = new Claim();
        claim.setClaimId("CLM1001");
        claim.setPatientId("PA1001");

        when(claimRepository.findById("CLM1001")).thenReturn(Optional.of(claim));

        Claim result = claimService.getClaimById("CLM1001");

        assertNotNull(result);
        assertEquals("CLM1001", result.getClaimId());
    }

    @Test
    void getClaimById_notFound_throwsException() {
        when(claimRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(ClaimNotFoundException.class,
                () -> claimService.getClaimById("CLM9999"));
    }

    @Test
    void updateFinalBill_claimNotFound_throwsException() {
        when(claimRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(ClaimNotFoundException.class, () -> {
            UpdateFinalBillRequest request = new UpdateFinalBillRequest();
            claimService.updateFinalBill("CLM9999", request, "HU1001");
        });
    }

    @Test
    void updateFinalBill_wrongStatus_throwsInvalidClaimStateException() {
        Claim claim = new Claim();
        claim.setClaimId("CLM1001");
        claim.setClaimStatus("SETTLED");

        when(claimRepository.findById("CLM1001")).thenReturn(Optional.of(claim));

        UpdateFinalBillRequest request = new UpdateFinalBillRequest();
        request.setFinalBillAmount(BigDecimal.valueOf(10000));

        assertThrows(InvalidClaimStateException.class,
                () -> claimService.updateFinalBill("CLM1001", request, "HU1001"));
    }

    @Test
    void updateFinalBill_withinPreauth_autoSettles() {
        Claim claim = new Claim();
        claim.setClaimId("CLM1001");
        claim.setClaimStatus("PREAUTH_APPROVED");
        claim.setPreauthorizedAmount(BigDecimal.valueOf(50000));

        when(claimRepository.findById("CLM1001")).thenReturn(Optional.of(claim));
        when(claimRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateFinalBillRequest request = new UpdateFinalBillRequest();
        request.setFinalBillAmount(BigDecimal.valueOf(40000));

        Claim result = claimService.updateFinalBill("CLM1001", request, "HU1001");

        assertEquals("SETTLED", result.getClaimStatus());
    }

    @Test
    void updateFinalBill_exceedsPreauth_setsUnderReview() {
        Claim claim = new Claim();
        claim.setClaimId("CLM1001");
        claim.setClaimStatus("PREAUTH_APPROVED");
        claim.setPreauthorizedAmount(BigDecimal.valueOf(50000));

        when(claimRepository.findById("CLM1001")).thenReturn(Optional.of(claim));
        when(claimRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateFinalBillRequest request = new UpdateFinalBillRequest();
        request.setFinalBillAmount(BigDecimal.valueOf(60000));

        Claim result = claimService.updateFinalBill("CLM1001", request, "HU1001");

        assertEquals("UNDER_REVIEW", result.getClaimStatus());
    }

    @Test
    void processClaim_claimNotFound_throwsException() {
        when(claimRepository.findById(anyString())).thenReturn(Optional.empty());

        ProcessClaimRequest request = new ProcessClaimRequest();
        request.setDecision("SETTLED");

        assertThrows(ClaimNotFoundException.class,
                () -> claimService.processClaim("CLM9999", request, "IU1001"));
    }

    @Test
    void processClaim_wrongStatus_throwsInvalidClaimStateException() {
        Claim claim = new Claim();
        claim.setClaimId("CLM1001");
        claim.setClaimStatus("PREAUTH_APPROVED");

        when(claimRepository.findById("CLM1001")).thenReturn(Optional.of(claim));

        ProcessClaimRequest request = new ProcessClaimRequest();
        request.setDecision("SETTLED");

        assertThrows(InvalidClaimStateException.class,
                () -> claimService.processClaim("CLM1001", request, "IU1001"));
    }

    @Test
    void processClaim_settledDecision_success() {
        Claim claim = new Claim();
        claim.setClaimId("CLM1001");
        claim.setClaimStatus("UNDER_REVIEW");
        claim.setInsuranceId("INS1001");
        claim.setFinalBillAmount(BigDecimal.valueOf(50000));
        claim.setPreauthorizedAmount(BigDecimal.valueOf(40000));

        when(claimRepository.findById("CLM1001")).thenReturn(Optional.of(claim));
        when(claimRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(restTemplate.postForEntity(anyString(), any(), any())).thenReturn(
                new org.springframework.http.ResponseEntity<>(new CoverageDeductionResponse(), org.springframework.http.HttpStatus.OK));

        ProcessClaimRequest request = new ProcessClaimRequest();
        request.setDecision("SETTLED");
        request.setApprovedAmount(BigDecimal.valueOf(50000));
        request.setDeductible(BigDecimal.ZERO);

        Claim result = claimService.processClaim("CLM1001", request, "IU1001");

        assertEquals("SETTLED", result.getClaimStatus());
    }

    @Test
    void processClaim_deniedDecision_success() {
        Claim claim = new Claim();
        claim.setClaimId("CLM1001");
        claim.setClaimStatus("UNDER_REVIEW");
        claim.setFinalBillAmount(BigDecimal.valueOf(50000));
        claim.setPreauthorizedAmount(BigDecimal.valueOf(40000));

        when(claimRepository.findById("CLM1001")).thenReturn(Optional.of(claim));
        when(claimRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProcessClaimRequest request = new ProcessClaimRequest();
        request.setDecision("DENIED");

        Claim result = claimService.processClaim("CLM1001", request, "IU1001");

        assertEquals("DENIED", result.getClaimStatus());
    }

    @Test
    void processClaim_invalidDecision_throwsIllegalArgument() {
        Claim claim = new Claim();
        claim.setClaimId("CLM1001");
        claim.setClaimStatus("UNDER_REVIEW");

        when(claimRepository.findById("CLM1001")).thenReturn(Optional.of(claim));

        ProcessClaimRequest request = new ProcessClaimRequest();
        request.setDecision("INVALID_DECISION");

        assertThrows(IllegalArgumentException.class,
                () -> claimService.processClaim("CLM1001", request, "IU1001"));
    }
}
