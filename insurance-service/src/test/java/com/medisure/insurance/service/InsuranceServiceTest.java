package com.medisure.insurance.service;

import com.medisure.insurance.dto.ClaimDeductionRequest;
import com.medisure.insurance.dto.ClaimDeductionResponse;
import com.medisure.insurance.dto.InsuranceValidityRequest;
import com.medisure.insurance.dto.InsuranceValidityResponse;
import com.medisure.insurance.entity.Insurance;
import com.medisure.insurance.repository.InsuranceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InsuranceServiceTest {

    @Mock
    private InsuranceRepository insuranceRepository;

    @InjectMocks
    private InsuranceService insuranceService;

    private Insurance testInsurance;

    @BeforeEach
    void setUp() {
        testInsurance = new Insurance();
        testInsurance.setInsuranceId("INS1001");
        testInsurance.setName("John Doe");
        testInsurance.setCoverage(BigDecimal.valueOf(500000));
        testInsurance.setRemainingCoverage(BigDecimal.valueOf(500000));
        testInsurance.setStartDate(LocalDate.now());
        testInsurance.setEndDate(LocalDate.now().plusYears(1));
    }

    @Test
    void createInsurance_autoGeneratesId() {
        Insurance insurance = new Insurance();
        insurance.setName("Patient Name");
        insurance.setCoverage(BigDecimal.valueOf(100000));

        when(insuranceRepository.findAll()).thenReturn(new ArrayList<>());
        when(insuranceRepository.save(any())).thenAnswer(inv -> {
            Insurance ins = inv.getArgument(0);
            ins.setInsuranceId("INS1001");
            return ins;
        });

        Insurance result = insuranceService.createInsurance(insurance);

        assertNotNull(result.getInsuranceId());
        assertTrue(result.getInsuranceId().startsWith("INS"));
    }

    @Test
    void createInsurance_setsRemainingCoverageFromCoverage() {
        Insurance insurance = new Insurance();
        insurance.setName("Patient");
        insurance.setCoverage(BigDecimal.valueOf(250000));
        insurance.setRemainingCoverage(null);

        when(insuranceRepository.findAll()).thenReturn(new ArrayList<>());
        when(insuranceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Insurance result = insuranceService.createInsurance(insurance);

        assertEquals(BigDecimal.valueOf(250000), result.getRemainingCoverage());
    }

    @Test
    void verifyInsuranceValidity_insuranceNotFound_returnsNotValid() {
        when(insuranceRepository.findById(anyString())).thenReturn(Optional.empty());

        InsuranceValidityRequest request = new InsuranceValidityRequest();
        request.setInsuranceId("INS9999");

        InsuranceValidityResponse response = insuranceService.verifyInsuranceValidity(request);

        assertEquals("not valid", response.getStatus());
    }

    @Test
    void verifyInsuranceValidity_expiredInsurance_returnsExpired() {
        testInsurance.setEndDate(LocalDate.now().minusDays(1));

        when(insuranceRepository.findById("INS1001")).thenReturn(Optional.of(testInsurance));

        InsuranceValidityRequest request = new InsuranceValidityRequest();
        request.setInsuranceId("INS1001");

        InsuranceValidityResponse response = insuranceService.verifyInsuranceValidity(request);

        assertEquals("expired", response.getStatus());
    }

    @Test
    void verifyInsuranceValidity_insufficientCoverage_returnsNotValid() {
        testInsurance.setRemainingCoverage(BigDecimal.valueOf(5000));

        when(insuranceRepository.findById("INS1001")).thenReturn(Optional.of(testInsurance));

        InsuranceValidityRequest request = new InsuranceValidityRequest();
        request.setInsuranceId("INS1001");
        request.setEstimatedAmount(BigDecimal.valueOf(10000));

        InsuranceValidityResponse response = insuranceService.verifyInsuranceValidity(request);

        assertEquals("not valid", response.getStatus());
    }

    @Test
    void verifyInsuranceValidity_valid_returnsValid() {
        testInsurance.setRemainingCoverage(BigDecimal.valueOf(100000));

        when(insuranceRepository.findById("INS1001")).thenReturn(Optional.of(testInsurance));

        InsuranceValidityRequest request = new InsuranceValidityRequest();
        request.setInsuranceId("INS1001");
        request.setEstimatedAmount(BigDecimal.valueOf(50000));

        InsuranceValidityResponse response = insuranceService.verifyInsuranceValidity(request);

        assertEquals("valid", response.getStatus());
    }

    @Test
    void getAllInsurances_returnsList() {
        List<Insurance> insurances = new ArrayList<>();
        insurances.add(testInsurance);
        when(insuranceRepository.findAll()).thenReturn(insurances);

        List<Insurance> result = insuranceService.getAllInsurances();

        assertEquals(1, result.size());
    }

    @Test
    void getInsuranceById_found() {
        when(insuranceRepository.findById("INS1001")).thenReturn(Optional.of(testInsurance));

        Optional<Insurance> result = insuranceService.getInsuranceById("INS1001");

        assertTrue(result.isPresent());
        assertEquals("INS1001", result.get().getInsuranceId());
    }

    @Test
    void getInsuranceById_notFound() {
        when(insuranceRepository.findById(anyString())).thenReturn(Optional.empty());

        Optional<Insurance> result = insuranceService.getInsuranceById("INS9999");

        assertFalse(result.isPresent());
    }

    @Test
    void deductCoverage_success() {
        testInsurance.setRemainingCoverage(BigDecimal.valueOf(100000));

        when(insuranceRepository.findById("INS1001")).thenReturn(Optional.of(testInsurance));
        when(insuranceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ClaimDeductionRequest request = new ClaimDeductionRequest();
        request.setAmount(BigDecimal.valueOf(20000));

        ClaimDeductionResponse response = insuranceService.deductCoverage("INS1001", request);

        assertNotNull(response);
        assertEquals("valid", response.getStatus());
        assertTrue(response.getRemainingCoverage().compareTo(BigDecimal.valueOf(80000)) >= 0);
    }
}
