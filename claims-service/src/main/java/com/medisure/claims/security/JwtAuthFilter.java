package com.medisure.claims.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String USER_TYPE_HOSPITAL = "HOSPITAL_USER";
    private static final String USER_TYPE_INSURANCE = "INSURANCE_USER";

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${hospital.service.base-url}")
    private String hospitalServiceBaseUrl;

    @Value("${insurance.service.base-url}")
    private String insuranceServiceBaseUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        // Allow CORS preflight requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

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
        String linkedUserId = jwtUtil.getLinkedUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);

        if (!USER_TYPE_HOSPITAL.equals(userType) && !USER_TYPE_INSURANCE.equals(userType)) {
            sendError(response, 403, "Forbidden", "Access denied. Invalid user type.");
            return;
        }

        // Check user status by calling the respective service
        if (!isUserActive(userType, linkedUserId)) {
            sendError(response, 403, "Forbidden", "Access denied. Your account has been revoked. Please contact the administrator.");
            return;
        }

        // Role-based endpoint access control
        String path = request.getRequestURI();
        String method = request.getMethod();

        if (!isEndpointAllowed(userType, method, path)) {
            String roleName = USER_TYPE_HOSPITAL.equals(userType) ? "Hospital" : "Insurance";
            sendError(response, 403, "Forbidden",
                    "Access denied. " + roleName + " users do not have permission to access this endpoint.");
            return;
        }

        // Set user info in request attributes
        request.setAttribute("username", username);
        request.setAttribute("userType", userType);
        request.setAttribute("linkedUserId", linkedUserId);

        filterChain.doFilter(request, response);
    }

    /**
     * Check if the user is ACTIVE by calling the respective service.
     */
    private boolean isUserActive(String userType, String linkedUserId) {
        try {
            if (USER_TYPE_HOSPITAL.equals(userType)) {
                String url = hospitalServiceBaseUrl + "/api/hospital/users/" + linkedUserId;
                Map<?, ?> user = restTemplate.getForObject(url, Map.class);
                return user != null && "ACTIVE".equals(user.get("status"));
            } else {
                String url = insuranceServiceBaseUrl + "/api/insurance/users/" + linkedUserId;
                Map<?, ?> user = restTemplate.getForObject(url, Map.class);
                return user != null && "ACTIVE".equals(user.get("status"));
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Role-based access control for claims endpoints.
     *
     * HOSPITAL_USER can:
     *   - POST   /api/claims          (create claim)
     *   - GET    /api/claims           (get all claims)
     *   - GET    /api/claims/{id}      (get claim by id)
     *   - PUT    /api/claims/{id}/final-bill (upload final bill)
     *
     * INSURANCE_USER can:
     *   - GET    /api/claims           (get all claims)
     *   - GET    /api/claims/{id}      (get claim by id)
     *   - POST   /api/claims/{id}/process  (process claim)
     */
    private boolean isEndpointAllowed(String userType, String method, String path) {
        if (USER_TYPE_HOSPITAL.equals(userType)) {
            // GET /api/claims or /api/claims/{id}
            if ("GET".equalsIgnoreCase(method) && path.startsWith("/api/claims")) {
                return true;
            }
            // POST /api/claims (create claim) — but NOT /api/claims/{id}/process
            if ("POST".equalsIgnoreCase(method) && "/api/claims".equals(path)) {
                return true;
            }
            // PUT /api/claims/{id}/final-bill
            if ("PUT".equalsIgnoreCase(method) && path.matches("/api/claims/[^/]+/final-bill")) {
                return true;
            }
            return false;
        }

        if (USER_TYPE_INSURANCE.equals(userType)) {
            // GET /api/claims or /api/claims/{id}
            if ("GET".equalsIgnoreCase(method) && path.startsWith("/api/claims")) {
                return true;
            }
            // POST /api/claims/{id}/process
            if ("POST".equalsIgnoreCase(method) && path.matches("/api/claims/[^/]+/process")) {
                return true;
            }
            return false;
        }

        return false;
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
