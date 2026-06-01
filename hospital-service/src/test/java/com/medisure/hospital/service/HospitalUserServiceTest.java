package com.medisure.hospital.service;

import com.medisure.hospital.dto.CreateHospitalUserRequest;
import com.medisure.hospital.dto.UpdateHospitalUserRequest;
import com.medisure.hospital.entity.HospitalUser;
import com.medisure.hospital.repository.HospitalUserRepository;
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
class HospitalUserServiceTest {

    @Mock
    private HospitalUserRepository userRepository;

    @InjectMocks
    private HospitalUserService hospitalUserService;

    private CreateHospitalUserRequest createRequest;

    @BeforeEach
    void setUp() {
        createRequest = new CreateHospitalUserRequest();
        createRequest.setUsername("dr.priya");
        createRequest.setName("Dr. Priya Sharma");
        createRequest.setHospitalName("City Hospital");
        createRequest.setEmail("priya@hospital.com");
        createRequest.setPhoneNo("9876543210");
        createRequest.setRole("DOCTOR");
    }

    @Test
    void createUser_success() {
        when(userRepository.existsByUsername("dr.priya")).thenReturn(false);
        when(userRepository.findAll()).thenReturn(new ArrayList<>());
        when(userRepository.save(any(HospitalUser.class))).thenAnswer(inv -> {
            HospitalUser user = inv.getArgument(0);
            user.setUserId("HU1001");
            return user;
        });

        HospitalUser result = hospitalUserService.createUser(createRequest);

        assertNotNull(result);
        assertEquals("dr.priya", result.getUsername());
        assertEquals("DOCTOR", result.getRole());
        assertEquals("ACTIVE", result.getStatus());
        assertTrue(result.getUserId().startsWith("HU"));

        verify(userRepository).save(any(HospitalUser.class));
    }

    @Test
    void createUser_duplicateUsername_throwsException() {
        when(userRepository.existsByUsername("dr.priya")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> hospitalUserService.createUser(createRequest));

        assertTrue(ex.getMessage().contains("already exists"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_withoutRole_setsDefaultStaffRole() {
        createRequest.setRole(null);
        when(userRepository.existsByUsername("dr.priya")).thenReturn(false);
        when(userRepository.findAll()).thenReturn(new ArrayList<>());
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        HospitalUser result = hospitalUserService.createUser(createRequest);

        assertEquals("STAFF", result.getRole());
    }

    @Test
    void getAllUsers_returnsList() {
        List<HospitalUser> users = new ArrayList<>();
        users.add(new HospitalUser());
        when(userRepository.findAll()).thenReturn(users);

        List<HospitalUser> result = hospitalUserService.getAllUsers();

        assertEquals(1, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_found() {
        HospitalUser user = new HospitalUser();
        user.setUserId("HU1001");
        user.setUsername("dr.priya");

        when(userRepository.findById("HU1001")).thenReturn(Optional.of(user));

        Optional<HospitalUser> result = hospitalUserService.getUserById("HU1001");

        assertTrue(result.isPresent());
        assertEquals("dr.priya", result.get().getUsername());
    }

    @Test
    void getUserById_notFound() {
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());

        Optional<HospitalUser> result = hospitalUserService.getUserById("HU9999");

        assertFalse(result.isPresent());
    }

    @Test
    void getUserById_normalizesCaseInsensitiveuserId() {
        HospitalUser user = new HospitalUser();
        user.setUserId("HU1001");

        when(userRepository.findById("HU1001")).thenReturn(Optional.of(user));

        hospitalUserService.getUserById("hu1001");

        verify(userRepository).findById("HU1001");
    }

    @Test
    void updateUser_success() {
        HospitalUser existingUser = new HospitalUser();
        existingUser.setUserId("HU1001");
        existingUser.setUsername("dr.priya");
        existingUser.setName("Dr. Priya");
        existingUser.setRole("DOCTOR");

        when(userRepository.findById("HU1001")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateHospitalUserRequest updateRequest = new UpdateHospitalUserRequest();
        updateRequest.setName("Dr. Priya Sharma Updated");
        updateRequest.setRole("STAFF");

        Optional<HospitalUser> result = hospitalUserService.updateUser("HU1001", updateRequest);

        assertTrue(result.isPresent());
        assertEquals("Dr. Priya Sharma Updated", result.get().getName());
        assertEquals("STAFF", result.get().getRole());
    }

    @Test
    void updateUser_partialUpdate() {
        HospitalUser existingUser = new HospitalUser();
        existingUser.setName("Old Name");
        existingUser.setEmail("old@email.com");

        when(userRepository.findById("HU1001")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateHospitalUserRequest updateRequest = new UpdateHospitalUserRequest();
        updateRequest.setName("New Name");

        Optional<HospitalUser> result = hospitalUserService.updateUser("HU1001", updateRequest);

        assertEquals("New Name", result.get().getName());
        assertEquals("old@email.com", result.get().getEmail());
    }

    @Test
    void updateUserStatus_toActive_success() {
        HospitalUser user = new HospitalUser();
        user.setStatus("REVOKED");

        when(userRepository.findById("HU1001")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Optional<HospitalUser> result = hospitalUserService.updateUserStatus("HU1001", "ACTIVE");

        assertTrue(result.isPresent());
        assertEquals("ACTIVE", result.get().getStatus());
    }

    @Test
    void updateUserStatus_toRevoked_success() {
        HospitalUser user = new HospitalUser();
        user.setStatus("ACTIVE");

        when(userRepository.findById("HU1001")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Optional<HospitalUser> result = hospitalUserService.updateUserStatus("HU1001", "REVOKED");

        assertTrue(result.isPresent());
        assertEquals("REVOKED", result.get().getStatus());
    }

    @Test
    void updateUserStatus_invalidStatus_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> hospitalUserService.updateUserStatus("HU1001", "INVALID"));

        assertTrue(ex.getMessage().contains("Invalid status"));
        assertTrue(ex.getMessage().contains("ACTIVE") && ex.getMessage().contains("REVOKED"));
    }

    @Test
    void updateUserStatus_caseInsensitive() {
        HospitalUser user = new HospitalUser();
        when(userRepository.findById("HU1001")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        hospitalUserService.updateUserStatus("HU1001", "active");

        verify(userRepository).save(any());
    }
}
