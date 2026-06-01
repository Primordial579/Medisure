package com.medisure.hospital.controller;

import com.medisure.hospital.dto.CreateHospitalUserRequest;
import com.medisure.hospital.dto.UpdateHospitalUserRequest;
import com.medisure.hospital.dto.UpdateStatusRequest;
import com.medisure.hospital.entity.HospitalUser;
import com.medisure.hospital.service.HospitalUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospital/users")
public class HospitalUserController {

    @Autowired
    private HospitalUserService userService;

    @PostMapping
    public ResponseEntity<HospitalUser> createUser(@RequestBody CreateHospitalUserRequest request) {
        HospitalUser createdUser = userService.createUser(request);
        return ResponseEntity.ok(createdUser);
    }

    @GetMapping
    public ResponseEntity<List<HospitalUser>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<HospitalUser> getUserById(@PathVariable("userId") String userId) {
        return userService.getUserById(userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{userId}")
    public ResponseEntity<HospitalUser> updateUser(@PathVariable("userId") String userId,
                                                    @RequestBody UpdateHospitalUserRequest request) {
        return userService.updateUser(userId, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<HospitalUser> updateUserStatus(@PathVariable("userId") String userId,
                                                          @RequestBody UpdateStatusRequest request) {
        return userService.updateUserStatus(userId, request.getStatus())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
