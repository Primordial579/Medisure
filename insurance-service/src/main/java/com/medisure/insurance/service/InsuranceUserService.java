package com.medisure.insurance.service;

import com.medisure.insurance.dto.CreateInsuranceUserRequest;
import com.medisure.insurance.dto.UpdateInsuranceUserRequest;
import com.medisure.insurance.entity.InsuranceUser;
import com.medisure.insurance.repository.InsuranceUserRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class InsuranceUserService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_REVOKED = "REVOKED";

    private final InsuranceUserRepository userRepository;

    public InsuranceUserService(InsuranceUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public InsuranceUser createUser(CreateInsuranceUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' already exists");
        }

        InsuranceUser user = new InsuranceUser();
        user.setUserId(generateUserId());
        user.setUsername(request.getUsername());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhoneNo(request.getPhoneNo());
        user.setRole(request.getRole() != null ? request.getRole() : "AGENT");
        user.setStatus(STATUS_ACTIVE);

        return userRepository.save(user);
    }

    public List<InsuranceUser> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<InsuranceUser> getUserById(String userId) {
        return userRepository.findById(normalizeUserId(userId));
    }

    public Optional<InsuranceUser> updateUser(String userId, UpdateInsuranceUserRequest request) {
        return userRepository.findById(normalizeUserId(userId))
                .map(existingUser -> {
                    if (request.getName() != null) {
                        existingUser.setName(request.getName());
                    }
                    if (request.getEmail() != null) {
                        existingUser.setEmail(request.getEmail());
                    }
                    if (request.getPhoneNo() != null) {
                        existingUser.setPhoneNo(request.getPhoneNo());
                    }
                    if (request.getRole() != null) {
                        existingUser.setRole(request.getRole());
                    }
                    return userRepository.save(existingUser);
                });
    }

    public Optional<InsuranceUser> updateUserStatus(String userId, String status) {
        String normalizedStatus = status.trim().toUpperCase();
        if (!STATUS_ACTIVE.equals(normalizedStatus) && !STATUS_REVOKED.equals(normalizedStatus)) {
            throw new IllegalArgumentException("Invalid status: " + status + ". Allowed values: ACTIVE, REVOKED");
        }

        return userRepository.findById(normalizeUserId(userId))
                .map(existingUser -> {
                    existingUser.setStatus(normalizedStatus);
                    return userRepository.save(existingUser);
                });
    }

    private String generateUserId() {
        long sequence = userRepository.findAll().stream()
                .map(InsuranceUser::getUserId)
                .filter(id -> id != null && id.startsWith("IU"))
                .map(id -> id.substring(2))
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

        String userId = "IU" + sequence;

        while (userRepository.existsById(userId)) {
            sequence++;
            userId = "IU" + sequence;
        }

        return userId;
    }

    private String normalizeUserId(String userId) {
        if (userId == null) {
            return "";
        }
        return userId.trim().toUpperCase();
    }
}
