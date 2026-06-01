package com.medisure.hospital.security;

import com.medisure.hospital.entity.HospitalUser;
import com.medisure.hospital.repository.HospitalUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String USER_TYPE_HOSPITAL = "HOSPITAL_USER";
    private static final String STATUS_ACTIVE = "ACTIVE";

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private HospitalUserRepository hospitalUserRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        String authHeader = request.getHeader("Authorization");

        // Allow CORS preflight requests
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        // Allow inter-service calls to patient endpoints (used by claims-service)
        if (authHeader == null && path.startsWith("/api/hospital/patients/") && "GET".equalsIgnoreCase(method)) {
            return true;
        }

        // Allow unauthenticated access to hospital user creation (POST) and retrieval (GET)
        if (("POST".equalsIgnoreCase(method) || "GET".equalsIgnoreCase(method))
                && (path.equals("/api/hospital/users") || path.startsWith("/api/hospital/users/"))) {
            return true;
        }

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendError(response, 401, "Unauthorized", "Missing or invalid Authorization header. Please provide a valid Bearer token.");
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            sendError(response, 401, "Unauthorized", "Invalid or expired token. Please login again.");
            return;
        }

        String userType = jwtUtil.getUserTypeFromToken(token);
        if (!USER_TYPE_HOSPITAL.equals(userType)) {
            sendError(response, 403, "Forbidden", "Access denied. Only hospital users can access hospital service.");
            return;
        }

        String linkedUserId = jwtUtil.getLinkedUserIdFromToken(token);
        if (linkedUserId == null || linkedUserId.isBlank()) {
            sendError(response, 403, "Forbidden", "Access denied. No hospital user linked to this account.");
            return;
        }

        // Check user status in DB
        Optional<HospitalUser> userOpt = hospitalUserRepository.findById(linkedUserId);
        if (userOpt.isEmpty()) {
            sendError(response, 403, "Forbidden", "Access denied. Hospital user not found.");
            return;
        }

        HospitalUser user = userOpt.get();
        if (!STATUS_ACTIVE.equals(user.getStatus())) {
            sendError(response, 403, "Forbidden", "Access denied. Your account has been revoked. Please contact the administrator.");
            return;
        }

        // Set user info in request attributes for downstream use
        request.setAttribute("username", jwtUtil.getUsernameFromToken(token));
        request.setAttribute("userType", userType);
        request.setAttribute("linkedUserId", linkedUserId);

        filterChain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, int status, String error, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);
        body.put("timestamp", LocalDateTime.now().toString());

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
