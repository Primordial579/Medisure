package com.medisure.auth.service;

import com.medisure.auth.dto.AuthResponse;
import com.medisure.auth.dto.LoginRequest;
import com.medisure.auth.dto.RegisterRequest;
import com.medisure.auth.entity.AuthUser;
import com.medisure.auth.repository.AuthUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthUserRepository authUserRepository;

    @Mock
    private Argon2PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest hospitalRegisterRequest;
    private RegisterRequest insuranceRegisterRequest;

    @BeforeEach
    void setUp() {
        hospitalRegisterRequest = new RegisterRequest();
        hospitalRegisterRequest.setUsername("dr.priya");
        hospitalRegisterRequest.setPassword("pass1234");
        hospitalRegisterRequest.setEmail("priya@hospital.com");
        hospitalRegisterRequest.setPhoneNo("9876543210");
        hospitalRegisterRequest.setUserType("HOSPITAL_USER");
        hospitalRegisterRequest.setHospitalUserId("HU1001");

        insuranceRegisterRequest = new RegisterRequest();
        insuranceRegisterRequest.setUsername("agent.raj");
        insuranceRegisterRequest.setPassword("pass1234");
        insuranceRegisterRequest.setEmail("raj@insurance.com");
        insuranceRegisterRequest.setPhoneNo("9876543211");
        insuranceRegisterRequest.setUserType("INSURANCE_USER");
        insuranceRegisterRequest.setInsuranceUserId("IU1001");
    }

    // ===================== register() tests =====================

    @Test
    void register_hospitalUser_success() {
        when(authUserRepository.existsByUsername("dr.priya")).thenReturn(false);
        when(passwordEncoder.encode("pass1234")).thenReturn("encoded_password");
        when(authUserRepository.save(any(AuthUser.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse response = authService.register(hospitalRegisterRequest);

        assertNotNull(response);
        assertNull(response.getToken());
        assertEquals("dr.priya", response.getUsername());
        assertEquals("HOSPITAL_USER", response.getUserType());
        assertEquals("HU1001", response.getLinkedUserId());
        assertEquals("User registered successfully", response.getMessage());

        verify(authUserRepository).save(any(AuthUser.class));
    }

    @Test
    void register_insuranceUser_success() {
        when(authUserRepository.existsByUsername("agent.raj")).thenReturn(false);
        when(passwordEncoder.encode("pass1234")).thenReturn("encoded_password");
        when(authUserRepository.save(any(AuthUser.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse response = authService.register(insuranceRegisterRequest);

        assertNotNull(response);
        assertEquals("agent.raj", response.getUsername());
        assertEquals("INSURANCE_USER", response.getUserType());
        assertEquals("IU1001", response.getLinkedUserId());
    }

    @Test
    void register_duplicateUsername_throwsIllegalArgumentException() {
        when(authUserRepository.existsByUsername("dr.priya")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register(hospitalRegisterRequest));

        assertTrue(ex.getMessage().contains("already exists"));
        verify(authUserRepository, never()).save(any());
    }

    @Test
    void register_invalidUserType_throwsIllegalArgumentException() {
        hospitalRegisterRequest.setUserType("INVALID_TYPE");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register(hospitalRegisterRequest));

        assertTrue(ex.getMessage().contains("userType must be"));
    }

    @Test
    void register_hospitalUserWithoutHospitalUserId_throwsIllegalArgumentException() {
        hospitalRegisterRequest.setHospitalUserId(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register(hospitalRegisterRequest));

        assertTrue(ex.getMessage().contains("hospitalUserId is required"));
    }

    @Test
    void register_hospitalUserWithBlankHospitalUserId_throwsIllegalArgumentException() {
        hospitalRegisterRequest.setHospitalUserId("   ");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register(hospitalRegisterRequest));

        assertTrue(ex.getMessage().contains("hospitalUserId is required"));
    }

    @Test
    void register_insuranceUserWithoutInsuranceUserId_throwsIllegalArgumentException() {
        insuranceRegisterRequest.setInsuranceUserId(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register(insuranceRegisterRequest));

        assertTrue(ex.getMessage().contains("insuranceUserId is required"));
    }

    @Test
    void register_userTypeLowercase_isNormalizedSuccessfully() {
        hospitalRegisterRequest.setUserType("hospital_user");
        when(authUserRepository.existsByUsername("dr.priya")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(authUserRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse response = authService.register(hospitalRegisterRequest);
        assertEquals("HOSPITAL_USER", response.getUserType());
    }

    // ===================== login() tests =====================

    @Test
    void login_success() {
        AuthUser user = new AuthUser();
        user.setUsername("dr.priya");
        user.setPassword("encoded_password");
        user.setUserType("HOSPITAL_USER");
        user.setHospitalUserId("HU1001");

        when(authUserRepository.findByUsername("dr.priya")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass1234", "encoded_password")).thenReturn(true);
        when(jwtService.generateToken("dr.priya", "HOSPITAL_USER", "HU1001")).thenReturn("jwt_token");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("dr.priya");
        loginRequest.setPassword("pass1234");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("jwt_token", response.getToken());
        assertEquals("dr.priya", response.getUsername());
        assertEquals("HOSPITAL_USER", response.getUserType());
        assertEquals("HU1001", response.getLinkedUserId());
        assertEquals("Login successful", response.getMessage());
    }

    @Test
    void login_userNotFound_throwsIllegalArgumentException() {
        when(authUserRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("unknown");
        loginRequest.setPassword("pass1234");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.login(loginRequest));

        assertEquals("Invalid username or password", ex.getMessage());
    }

    @Test
    void login_wrongPassword_throwsIllegalArgumentException() {
        AuthUser user = new AuthUser();
        user.setUsername("dr.priya");
        user.setPassword("encoded_password");
        user.setUserType("HOSPITAL_USER");
        user.setHospitalUserId("HU1001");

        when(authUserRepository.findByUsername("dr.priya")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong_password", "encoded_password")).thenReturn(false);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("dr.priya");
        loginRequest.setPassword("wrong_password");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.login(loginRequest));

        assertEquals("Invalid username or password", ex.getMessage());
    }

    @Test
    void login_insuranceUser_returnsInsuranceLinkedId() {
        AuthUser user = new AuthUser();
        user.setUsername("agent.raj");
        user.setPassword("encoded_password");
        user.setUserType("INSURANCE_USER");
        user.setInsuranceUserId("IU1001");

        when(authUserRepository.findByUsername("agent.raj")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass1234", "encoded_password")).thenReturn(true);
        when(jwtService.generateToken("agent.raj", "INSURANCE_USER", "IU1001")).thenReturn("token");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("agent.raj");
        loginRequest.setPassword("pass1234");

        AuthResponse response = authService.login(loginRequest);

        assertEquals("IU1001", response.getLinkedUserId());
        assertEquals("INSURANCE_USER", response.getUserType());
    }
}
