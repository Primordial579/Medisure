package com.medisure.hospital.repository;

import com.medisure.hospital.entity.HospitalUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HospitalUserRepository extends JpaRepository<HospitalUser, String> {

    Optional<HospitalUser> findByUsername(String username);

    boolean existsByUsername(String username);
}
