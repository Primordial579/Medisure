package com.medisure.insurance.repository;

import com.medisure.insurance.entity.InsuranceUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InsuranceUserRepository extends JpaRepository<InsuranceUser, String> {

    Optional<InsuranceUser> findByUsername(String username);

    boolean existsByUsername(String username);
}
