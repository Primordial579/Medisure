package com.medisure.insurance.controller;

import com.medisure.insurance.dto.InsuranceValidityRequest;
import com.medisure.insurance.dto.InsuranceValidityResponse;
import com.medisure.insurance.entity.Insurance;
import com.medisure.insurance.service.InsuranceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.medisure.insurance.dto.ClaimDeductionRequest;
import com.medisure.insurance.dto.ClaimDeductionResponse;

@RestController
@RequestMapping("/api/insurance")
public class InsuranceController {

    private final InsuranceService insuranceService;

    public InsuranceController(InsuranceService insuranceService) {
        this.insuranceService = insuranceService;
    }

    @PostMapping({"/insurances", "/policies"})
    public ResponseEntity<Insurance> createInsurance(@RequestBody Insurance insurance) {
        Insurance createdInsurance = insuranceService.createInsurance(insurance);
        return ResponseEntity.ok(createdInsurance);
    }

    @GetMapping({"/insurances/{insuranceId}", "/policies/{insuranceId}"})
    public ResponseEntity<Insurance> getInsuranceById(@PathVariable("insuranceId") String insuranceId) {
        return insuranceService.getInsuranceById(insuranceId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping({"/insurances", "/policies"})
    public ResponseEntity<List<Insurance>> getAllInsurances() {
        return ResponseEntity.ok(insuranceService.getAllInsurances());
    }

    @PostMapping({"/insurances/verify", "/policies/verify"})
    public ResponseEntity<InsuranceValidityResponse> verifyInsuranceValidity(@Valid @RequestBody InsuranceValidityRequest request) {
        return ResponseEntity.ok(insuranceService.verifyInsuranceValidity(request));
    }

    @PostMapping({"/insurances/{insuranceId}/deduct", "/policies/{insuranceId}/deduct"})
    public ResponseEntity<ClaimDeductionResponse> deductCoverage(@PathVariable("insuranceId") String insuranceId,
                                                                  @Valid @RequestBody ClaimDeductionRequest request) {
        ClaimDeductionResponse resp = insuranceService.deductCoverage(insuranceId, request);
        return ResponseEntity.ok(resp);
    }
}