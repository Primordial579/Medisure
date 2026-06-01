package com.medisure.insurance.controller;

import com.medisure.insurance.dto.CreateInsuranceUserRequest;
import com.medisure.insurance.dto.UpdateInsuranceUserRequest;
import com.medisure.insurance.dto.UpdateStatusRequest;
import com.medisure.insurance.entity.InsuranceUser;
import com.medisure.insurance.service.InsuranceUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/insurance/users")
public class InsuranceUserController {

    private final InsuranceUserService userService;

    public InsuranceUserController(InsuranceUserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<InsuranceUser> createUser(@RequestBody CreateInsuranceUserRequest request) {
        InsuranceUser createdUser = userService.createUser(request);
        return ResponseEntity.ok(createdUser);
    }

    @GetMapping
    public ResponseEntity<List<InsuranceUser>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<InsuranceUser> getUserById(@PathVariable("userId") String userId) {
        return userService.getUserById(userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{userId}")
    public ResponseEntity<InsuranceUser> updateUser(@PathVariable("userId") String userId,
                                                     @RequestBody UpdateInsuranceUserRequest request) {
        return userService.updateUser(userId, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<InsuranceUser> updateUserStatus(@PathVariable("userId") String userId,
                                                           @RequestBody UpdateStatusRequest request) {
        return userService.updateUserStatus(userId, request.getStatus())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
