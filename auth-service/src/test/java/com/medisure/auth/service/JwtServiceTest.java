package com.medisure.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET = "MediSureSuperSecretKeyForJWTTokenGeneration2026TestOnly";
    private static final long EXPIRATION_MS = 3600000L;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", EXPIRATION_MS);
    }

    @Test
    void generateToken_shouldReturnNonBlankToken() {
        String token = jwtService.generateToken("testUser", "HOSPITAL_USER", "HU1001");
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void validateToken_validToken_returnsTrue() {
        String token = jwtService.generateToken("testUser", "HOSPITAL_USER", "HU1001");
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void validateToken_invalidToken_returnsFalse() {
        assertFalse(jwtService.validateToken("this.is.invalid"));
    }

    @Test
    void validateToken_emptyToken_returnsFalse() {
        assertFalse(jwtService.validateToken(""));
    }

    @Test
    void getUsernameFromToken_shouldReturnCorrectUsername() {
        String token = jwtService.generateToken("drPriya", "HOSPITAL_USER", "HU1001");
        assertEquals("drPriya", jwtService.getUsernameFromToken(token));
    }

    @Test
    void getUserTypeFromToken_shouldReturnCorrectUserType() {
        String token = jwtService.generateToken("agent", "INSURANCE_USER", "IU1001");
        assertEquals("INSURANCE_USER", jwtService.getUserTypeFromToken(token));
    }

    @Test
    void getLinkedUserIdFromToken_shouldReturnCorrectLinkedUserId() {
        String token = jwtService.generateToken("user", "HOSPITAL_USER", "HU2001");
        assertEquals("HU2001", jwtService.getLinkedUserIdFromToken(token));
    }

    @Test
    void generateToken_forInsuranceUser_shouldContainInsuranceUserType() {
        String token = jwtService.generateToken("agentRaj", "INSURANCE_USER", "IU1001");
        assertEquals("INSURANCE_USER", jwtService.getUserTypeFromToken(token));
        assertEquals("IU1001", jwtService.getLinkedUserIdFromToken(token));
    }
}
