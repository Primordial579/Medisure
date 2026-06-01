package com.medisure.auth.service;

import com.medisure.auth.dto.AuthResponse;
import com.medisure.auth.dto.LoginRequest;
import com.medisure.auth.dto.RegisterRequest;
import com.medisure.auth.entity.AuthUser;
import com.medisure.auth.repository.AuthUserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final String USER_TYPE_HOSPITAL = "HOSPITAL_USER";
    private static final String USER_TYPE_INSURANCE = "INSURANCE_USER";

    private final AuthUserRepository authUserRepository;
    private final Argon2PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AuthUserRepository authUserRepository,
                       Argon2PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.authUserRepository = authUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (authUserRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' already exists");
        }

        String userType = request.getUserType() != null ? request.getUserType().toUpperCase() : "";
        if (!USER_TYPE_HOSPITAL.equals(userType) && !USER_TYPE_INSURANCE.equals(userType)) {
            throw new IllegalArgumentException("userType must be HOSPITAL_USER or INSURANCE_USER");
        }

        if (USER_TYPE_HOSPITAL.equals(userType)) {
            if (request.getHospitalUserId() == null || request.getHospitalUserId().isBlank()) {
                throw new IllegalArgumentException("hospitalUserId is required for HOSPITAL_USER");
            }
        } else {
            if (request.getInsuranceUserId() == null || request.getInsuranceUserId().isBlank()) {
                throw new IllegalArgumentException("insuranceUserId is required for INSURANCE_USER");
            }
        }

        AuthUser user = new AuthUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhoneNo(request.getPhoneNo());
        user.setUserType(userType);
        user.setHospitalUserId(request.getHospitalUserId());
        user.setInsuranceUserId(request.getInsuranceUserId());

        authUserRepository.save(user);

        String linkedUserId = USER_TYPE_HOSPITAL.equals(userType)
                ? user.getHospitalUserId() : user.getInsuranceUserId();

        return new AuthResponse(null, user.getUsername(), userType, linkedUserId,
                "User registered successfully");
    }

    public AuthResponse login(LoginRequest request) {
        AuthUser user = authUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        String linkedUserId = USER_TYPE_HOSPITAL.equals(user.getUserType())
                ? user.getHospitalUserId() : user.getInsuranceUserId();

        String token = jwtService.generateToken(user.getUsername(), user.getUserType(), linkedUserId);

        return new AuthResponse(token, user.getUsername(), user.getUserType(), linkedUserId,
                "Login successful");
    }
}
