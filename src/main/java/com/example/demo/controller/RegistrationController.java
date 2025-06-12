package com.example.demo.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.User;

import com.example.demo.service.RegistrationService;

@Controller
@RequestMapping("/register")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;
    private com.example.demo.model.User user;

    @GetMapping
    public String showRegistrationForm(Model model, @RequestParam(required = false) String error) {
        model.addAttribute("user", new com.example.demo.model.User());
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "register";
    }

    @PostMapping
    public String registerUser(@ModelAttribute("user") com.example.demo.model.User user) {
        try {
            PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
            // Encrypt password
            user.setPassword(encoder.encode(user.getPassword()));
            user.setPin(encoder.encode(user.getPin()));
            user.setRole("CUSTOMER");
            registrationService.register(user);

            // Add user to in-memory user details manager
            InMemoryUserDetailsManager inMemoryUserDetailsManager = new InMemoryUserDetailsManager();
            inMemoryUserDetailsManager.createUser(User.withUsername(user.getEmail()).password(user.getPassword())
                    .roles(user.getRole())
                    .build());
            return "redirect:/";
        } catch (Exception e) {
            try {
                return "redirect:/register?error=" + URLEncoder.encode(e.getMessage(), "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                return "redirect:/register?error=Registration failed";
            }
        }
    }
}
