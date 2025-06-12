package com.example.demo.controller;

import com.example.demo.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.config.CustomAuthenticationFailureHandler;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;

@RestController
public class AuthController {

    private AuthenticationManager authenticationManager;
    private CustomAuthenticationFailureHandler authenticationFailureHandler;
    private JwtUtil jwtUtil;
    private SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder
            .getContextHolderStrategy();

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
            CustomAuthenticationFailureHandler authenticationFailureHandler) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.authenticationFailureHandler = authenticationFailureHandler;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> customLogin(@RequestBody Map<String, String> loginRequest,
            HttpServletRequest request, HttpServletResponse response) {

        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            String token = jwtUtil.generateToken(userDetails.getUsername());

            // Save the authentication to the security context
            SecurityContext context = securityContextHolderStrategy.createEmptyContext();
            context.setAuthentication(authentication);
            securityContextHolderStrategy.setContext(context);
            securityContextRepository.saveContext(context, request, response);
            return ResponseEntity.ok(Map.of("token", token));
        } catch (AuthenticationException e) {
            try {
                authenticationFailureHandler.onAuthenticationFailure(request, response, e);
            } catch (Exception ex) {
                return ResponseEntity.status(401).body("Invalid username or password");
            }
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }
}