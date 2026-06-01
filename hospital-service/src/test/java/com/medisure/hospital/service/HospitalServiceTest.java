package com.medisure.hospital.service;

import com.medisure.hospital.entity.Patient;
import com.medisure.hospital.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HospitalServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private HospitalService hospitalService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void createPatient_withBlankPatientId_generatesPatientId() {
        Patient patient = new Patient();
        patient.setPatientId("");
        patient.setName("John Doe");
        patient.setDob(LocalDate.of(1990, 5, 15));
        patient.setPhoneNo("9876543210");

        when(patientRepository.findAll()).thenReturn(new ArrayList<>());
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> {
            Patient p = inv.getArgument(0);
            p.setPatientId("PA1001");
            return p;
        });

        Patient result = hospitalService.createPatient(patient, "HU1001");

        assertNotNull(result.getPatientId());
        assertTrue(result.getPatientId().startsWith("PA"));
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void createPatient_withProvidedPatientId_usesProvidedId() {
        Patient patient = new Patient();
        patient.setPatientId("PA5001");
        patient.setName("Jane Doe");

        when(patientRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Patient result = hospitalService.createPatient(patient, "HU1001");

        assertEquals("PA5001", result.getPatientId());
    }

    @Test
    void createPatient_nullPatientId_generatesPatientId() {
        Patient patient = new Patient();
        patient.setPatientId(null);
        patient.setName("Test Patient");

        when(patientRepository.findAll()).thenReturn(new ArrayList<>());
        when(patientRepository.save(any())).thenAnswer(inv -> {
            Patient p = inv.getArgument(0);
            p.setPatientId("PA1001");
            return p;
        });

        Patient result = hospitalService.createPatient(patient, "HU1001");

        assertNotNull(result.getPatientId());
        assertTrue(result.getPatientId().startsWith("PA"));
    }

    @Test
    void getAllPatients_returnsList() {
        List<Patient> patients = new ArrayList<>();
        patients.add(new Patient());
        when(patientRepository.findAll()).thenReturn(patients);

        List<Patient> result = hospitalService.getAllPatients();

        assertEquals(1, result.size());
        verify(patientRepository).findAll();
    }

    @Test
    void getPatientById_found() {
        Patient patient = new Patient();
        patient.setPatientId("PA1001");
        patient.setName("John Doe");

        when(patientRepository.findById("PA1001")).thenReturn(Optional.of(patient));

        Optional<Patient> result = hospitalService.getPatientById("PA1001");

        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
    }

    @Test
    void getPatientById_notFound() {
        when(patientRepository.findById(anyString())).thenReturn(Optional.empty());

        Optional<Patient> result = hospitalService.getPatientById("PA9999");

        assertFalse(result.isPresent());
    }

    @Test
    void getPatientById_caseInsensitive() {
        Patient patient = new Patient();
        patient.setPatientId("PA1001");

        when(patientRepository.findById("PA1001")).thenReturn(Optional.of(patient));

        hospitalService.getPatientById("pa1001");

        verify(patientRepository).findById("PA1001");
    }

    @Test
    void updatePatient_found_updatesAllFields() {
        Patient existingPatient = new Patient();
        existingPatient.setPatientId("PA1001");
        existingPatient.setName("Old Name");
        existingPatient.setPhoneNo("9876543210");
        existingPatient.setEmail("old@email.com");
        existingPatient.setBloodGroup("O+");

        when(patientRepository.findById("PA1001")).thenReturn(Optional.of(existingPatient));
        when(patientRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Patient updatePatient = new Patient();
        updatePatient.setName("New Name");
        updatePatient.setPhoneNo("9999999999");
        updatePatient.setEmail("new@email.com");
        updatePatient.setBloodGroup("AB-");

        Optional<Patient> result = hospitalService.updatePatient("PA1001", updatePatient, "HU1001");

        assertTrue(result.isPresent());
        assertEquals("New Name", result.get().getName());
        assertEquals("9999999999", result.get().getPhoneNo());
        assertEquals("new@email.com", result.get().getEmail());
        assertEquals("AB-", result.get().getBloodGroup());
    }

    @Test
    void updatePatient_notFound_returnsEmpty() {
        when(patientRepository.findById(anyString())).thenReturn(Optional.empty());

        Patient updatePatient = new Patient();
        updatePatient.setName("New Name");

        Optional<Patient> result = hospitalService.updatePatient("PA9999", updatePatient, "HU1001");

        assertFalse(result.isPresent());
    }

    @Test
    void updatePatient_caseInsensitiveId() {
        Patient patient = new Patient();
        patient.setPatientId("PA1001");

        when(patientRepository.findById("PA1001")).thenReturn(Optional.of(patient));
        when(patientRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Patient updatePatient = new Patient();
        hospitalService.updatePatient("pa1001", updatePatient, "HU1001");

        verify(patientRepository).findById("PA1001");
    }

    @Test
    void normalizePatientId_convertsToUppercase() {
        Patient patient = new Patient();
        patient.setPatientId("pa1001");

        when(patientRepository.findById("PA1001")).thenReturn(Optional.of(patient));

        hospitalService.getPatientById("pa1001");

        verify(patientRepository).findById("PA1001");
    }

    @Test
    void normalizePatientId_trimsBlanks() {
        Patient patient = new Patient();
        patient.setPatientId("PA1001");

        when(patientRepository.findById("PA1001")).thenReturn(Optional.of(patient));

        hospitalService.getPatientById("  pa1001  ");

        verify(patientRepository).findById("PA1001");
    }
}
