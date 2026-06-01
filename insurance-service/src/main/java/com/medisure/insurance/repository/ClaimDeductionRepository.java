package com.medisure.insurance.repository;

import com.medisure.insurance.entity.ClaimDeduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClaimDeductionRepository extends JpaRepository<ClaimDeduction, Long> {
    Optional<ClaimDeduction> findByClaimId(String claimId);
}
