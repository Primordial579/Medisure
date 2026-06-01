package com.medisure.hospital.service;

import com.medisure.hospital.dto.CreateHospitalUserRequest;
import com.medisure.hospital.dto.UpdateHospitalUserRequest;
import com.medisure.hospital.entity.HospitalUser;
import com.medisure.hospital.repository.HospitalUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class HospitalUserService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_REVOKED = "REVOKED";

    @Autowired
    private HospitalUserRepository userRepository;

    public HospitalUser createUser(CreateHospitalUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' already exists");
        }

        HospitalUser user = new HospitalUser();
        user.setUserId(generateUserId());
        user.setUsername(request.getUsername());
        user.setName(request.getName());
        user.setHospitalName(request.getHospitalName());
        user.setEmail(request.getEmail());
        user.setPhoneNo(request.getPhoneNo());
        user.setRole(request.getRole() != null ? request.getRole() : "STAFF");
        user.setStatus(STATUS_ACTIVE);

        return userRepository.save(user);
    }

    public List<HospitalUser> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<HospitalUser> getUserById(String userId) {
        return userRepository.findById(normalizeUserId(userId));
    }

    public Optional<HospitalUser> updateUser(String userId, UpdateHospitalUserRequest request) {
        return userRepository.findById(normalizeUserId(userId))
                .map(existingUser -> {
                    if (request.getName() != null) {
                        existingUser.setName(request.getName());
                    }
                    if (request.getHospitalName() != null) {
                        existingUser.setHospitalName(request.getHospitalName());
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

    public Optional<HospitalUser> updateUserStatus(String userId, String status) {
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
                .map(HospitalUser::getUserId)
                .filter(id -> id != null && id.startsWith("HU"))
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

        String userId = "HU" + sequence;

        while (userRepository.existsById(userId)) {
            sequence++;
            userId = "HU" + sequence;
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
