package com.medisure.claims.controller;

import com.medisure.claims.dto.CreateClaimRequest;
import com.medisure.claims.dto.ProcessClaimRequest;
import com.medisure.claims.dto.UpdateFinalBillRequest;
import com.medisure.claims.entity.Claim;
import com.medisure.claims.service.ClaimService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping
    public ResponseEntity<Claim> createClaim(@RequestBody CreateClaimRequest request, HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("linkedUserId");
        Claim claim = claimService.createClaim(request, userId);
        return ResponseEntity.ok(claim);
    }

    @GetMapping
    public ResponseEntity<List<Claim>> getAllClaims() {
        return ResponseEntity.ok(claimService.getAllClaims());
    }

    @GetMapping("/{claimId}")
    public ResponseEntity<Claim> getClaimById(@PathVariable("claimId") String claimId) {
        return ResponseEntity.ok(claimService.getClaimById(claimId));
    }

    @PutMapping("/{claimId}/final-bill")
    public ResponseEntity<Claim> uploadFinalBill(@PathVariable("claimId") String claimId,
                                                  @RequestBody UpdateFinalBillRequest request,
                                                  HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("linkedUserId");
        return ResponseEntity.ok(claimService.updateFinalBill(claimId, request, userId));
    }

    @PostMapping("/{claimId}/process")
    public ResponseEntity<Claim> processClaim(@PathVariable("claimId") String claimId,
                                               @RequestBody ProcessClaimRequest request,
                                               HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("linkedUserId");
        return ResponseEntity.ok(claimService.processClaim(claimId, request, userId));
    }
}
