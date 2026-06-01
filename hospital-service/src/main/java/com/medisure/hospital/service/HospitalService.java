package com.medisure.hospital.service;

import com.medisure.hospital.entity.Patient;
import com.medisure.hospital.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class HospitalService {

    @Autowired
    private PatientRepository patientRepository;

    public Patient createPatient(Patient patient, String userId) {
        if (patient.getPatientId() == null || patient.getPatientId().isBlank()) {
            patient.setPatientId(generatePatientId());
        }
        patient.setCreatedBy(userId);
        patient.setUpdatedBy(userId);
        return patientRepository.save(patient);
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Optional<Patient> getPatientById(String patientId) {
        return patientRepository.findById(normalizePatientId(patientId));
    }

    public Optional<Patient> updatePatient(String patientId, Patient patient, String userId) {
        return patientRepository.findById(normalizePatientId(patientId))
                .map(existingPatient -> {
                    existingPatient.setName(patient.getName());
                    existingPatient.setDob(patient.getDob());
                    existingPatient.setPhoneNo(patient.getPhoneNo());
                    existingPatient.setEmail(patient.getEmail());
                    existingPatient.setBloodGroup(patient.getBloodGroup());
                    existingPatient.setInsuranceId(patient.getInsuranceId());
                    existingPatient.setHealthReport(patient.getHealthReport());
                    existingPatient.setUpdatedBy(userId);
                    return patientRepository.save(existingPatient);
                });
    }

    private String generatePatientId() {
        long sequence = patientRepository.findAll().stream()
                .map(Patient::getPatientId)
                .filter(existingPatientId -> existingPatientId != null && existingPatientId.startsWith("PA"))
                .map(existingPatientId -> existingPatientId.substring(2))
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

        String patientId = "PA" + sequence;

        while (patientRepository.existsById(patientId)) {
            sequence++;
            patientId = "PA" + sequence;
        }

        return patientId;
    }

    private String normalizePatientId(String patientId) {
        if (patientId == null) {
            return "";
        }
        return patientId.trim().toUpperCase();
    }
}