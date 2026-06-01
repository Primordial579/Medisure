package com.medisure.hospital.controller;

import com.medisure.hospital.entity.Patient;
import com.medisure.hospital.service.HospitalService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospital")
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;

    @PostMapping("/patients")
    public ResponseEntity<Patient> createPatient(@RequestBody Patient patient, HttpServletRequest request) {
        String userId = (String) request.getAttribute("linkedUserId");
        Patient createdPatient = hospitalService.createPatient(patient, userId);
        return ResponseEntity.ok(createdPatient);
    }

    @GetMapping("/patients")
    public ResponseEntity<List<Patient>> getAllPatients() {
        return ResponseEntity.ok(hospitalService.getAllPatients());
    }

    @GetMapping("/patients/{patientId}")
    public ResponseEntity<Patient> getPatientById(@PathVariable("patientId") String patientId) {
        return hospitalService.getPatientById(patientId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/patients/{patientId}")
    public ResponseEntity<Patient> updatePatient(@PathVariable("patientId") String patientId, @RequestBody Patient patient, HttpServletRequest request) {
        String userId = (String) request.getAttribute("linkedUserId");
        return hospitalService.updatePatient(patientId, patient, userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}