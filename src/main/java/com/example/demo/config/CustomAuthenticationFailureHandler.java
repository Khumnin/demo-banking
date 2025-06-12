package com.example.demo.config;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        String errorMessage = "Invalid username or password";
        System.out.println("exception: " + exception);
        System.out.println("request: " + request);
        System.out.println("response: " + response);

        if (exception.getMessage().equalsIgnoreCase("User account is locked")) {
            errorMessage = "Your account is locked. Please contact support.";
        } else if (exception.getMessage().equalsIgnoreCase("User account is disabled")) {
            errorMessage = "Your account is disabled. Please contact support.";
        } else if (exception.getMessage().equalsIgnoreCase("User account has expired")) {
            errorMessage = "Your account has expired. Please contact support.";
        }

        // Check if it's an API request
        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader != null && acceptHeader.contains(MediaType.APPLICATION_JSON_VALUE)) {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(response.getOutputStream(), Map.of("location",
                    "/register?error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8") + "&showRegister=true"));
        } else {
            // For web form login, redirect to registration page
            response.sendRedirect(
                    "/register?error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8") + "&showRegister=true");
        }
    }
}