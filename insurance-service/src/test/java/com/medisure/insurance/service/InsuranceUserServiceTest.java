package com.medisure.insurance.service;

import com.medisure.insurance.dto.CreateInsuranceUserRequest;
import com.medisure.insurance.dto.UpdateInsuranceUserRequest;
import com.medisure.insurance.entity.InsuranceUser;
import com.medisure.insurance.repository.InsuranceUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InsuranceUserServiceTest {

    @Mock
    private InsuranceUserRepository userRepository;

    @InjectMocks
    private InsuranceUserService insuranceUserService;

    private CreateInsuranceUserRequest createRequest;

    @BeforeEach
    void setUp() {
        createRequest = new CreateInsuranceUserRequest();
        createRequest.setUsername("agent.raj");
        createRequest.setName("Raj Patel");
        createRequest.setEmail("raj@insurance.com");
        createRequest.setPhoneNo("9876543210");
        createRequest.setRole("UNDERWRITER");
    }

    @Test
    void createUser_success() {
        when(userRepository.existsByUsername("agent.raj")).thenReturn(false);
        when(userRepository.findAll()).thenReturn(new ArrayList<>());
        when(userRepository.save(any(InsuranceUser.class))).thenAnswer(inv -> {
            InsuranceUser user = inv.getArgument(0);
            user.setUserId("IU1001");
            return user;
        });

        InsuranceUser result = insuranceUserService.createUser(createRequest);

        assertNotNull(result);
        assertEquals("agent.raj", result.getUsername());
        assertEquals("UNDERWRITER", result.getRole());
        assertEquals("ACTIVE", result.getStatus());
        assertTrue(result.getUserId().startsWith("IU"));

        verify(userRepository).save(any(InsuranceUser.class));
    }

    @Test
    void createUser_duplicateUsername_throwsException() {
        when(userRepository.existsByUsername("agent.raj")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> insuranceUserService.createUser(createRequest));

        assertTrue(ex.getMessage().contains("already exists"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_withoutRole_setsDefaultAgentRole() {
        createRequest.setRole(null);
        when(userRepository.existsByUsername("agent.raj")).thenReturn(false);
        when(userRepository.findAll()).thenReturn(new ArrayList<>());
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InsuranceUser result = insuranceUserService.createUser(createRequest);

        assertEquals("AGENT", result.getRole());
    }

    @Test
    void getAllUsers_returnsList() {
        List<InsuranceUser> users = new ArrayList<>();
        users.add(new InsuranceUser());
        when(userRepository.findAll()).thenReturn(users);

        List<InsuranceUser> result = insuranceUserService.getAllUsers();

        assertEquals(1, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_found() {
        InsuranceUser user = new InsuranceUser();
        user.setUserId("IU1001");
        user.setUsername("agent.raj");

        when(userRepository.findById("IU1001")).thenReturn(Optional.of(user));

        Optional<InsuranceUser> result = insuranceUserService.getUserById("IU1001");

        assertTrue(result.isPresent());
        assertEquals("agent.raj", result.get().getUsername());
    }

    @Test
    void getUserById_notFound() {
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());

        Optional<InsuranceUser> result = insuranceUserService.getUserById("IU9999");

        assertFalse(result.isPresent());
    }

    @Test
    void updateUser_success() {
        InsuranceUser existingUser = new InsuranceUser();
        existingUser.setUserId("IU1001");
        existingUser.setUsername("agent.raj");
        existingUser.setName("Raj Patel");
        existingUser.setRole("UNDERWRITER");

        when(userRepository.findById("IU1001")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateInsuranceUserRequest updateRequest = new UpdateInsuranceUserRequest();
        updateRequest.setName("Raj Kumar Patel");
        updateRequest.setRole("SUPERVISOR");

        Optional<InsuranceUser> result = insuranceUserService.updateUser("IU1001", updateRequest);

        assertTrue(result.isPresent());
        assertEquals("Raj Kumar Patel", result.get().getName());
        assertEquals("SUPERVISOR", result.get().getRole());
    }

    @Test
    void updateUser_partialUpdate() {
        InsuranceUser existingUser = new InsuranceUser();
        existingUser.setName("Old Name");
        existingUser.setEmail("old@email.com");

        when(userRepository.findById("IU1001")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateInsuranceUserRequest updateRequest = new UpdateInsuranceUserRequest();
        updateRequest.setName("New Name");

        Optional<InsuranceUser> result = insuranceUserService.updateUser("IU1001", updateRequest);

        assertEquals("New Name", result.get().getName());
        assertEquals("old@email.com", result.get().getEmail());
    }

    @Test
    void updateUserStatus_toActive_success() {
        InsuranceUser user = new InsuranceUser();
        user.setStatus("REVOKED");

        when(userRepository.findById("IU1001")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Optional<InsuranceUser> result = insuranceUserService.updateUserStatus("IU1001", "ACTIVE");

        assertTrue(result.isPresent());
        assertEquals("ACTIVE", result.get().getStatus());
    }

    @Test
    void updateUserStatus_toRevoked_success() {
        InsuranceUser user = new InsuranceUser();
        user.setStatus("ACTIVE");

        when(userRepository.findById("IU1001")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Optional<InsuranceUser> result = insuranceUserService.updateUserStatus("IU1001", "REVOKED");

        assertTrue(result.isPresent());
        assertEquals("REVOKED", result.get().getStatus());
    }

    @Test
    void updateUserStatus_invalidStatus_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> insuranceUserService.updateUserStatus("IU1001", "INVALID"));

        assertTrue(ex.getMessage().contains("Invalid status"));
    }

    @Test
    void updateUserStatus_caseInsensitive() {
        InsuranceUser user = new InsuranceUser();
        when(userRepository.findById("IU1001")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        insuranceUserService.updateUserStatus("IU1001", "active");

        verify(userRepository).save(any());
    }
}
