package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataSeeder(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Override
    public void run(String... args) {
        // Seed Teller account
        if (!userRepository.existsById("teller@bank.com")) {
            User teller = new User();
            teller.setEmail("teller@bank.com");
            teller.setPassword(passwordEncoder.encode("password"));
            teller.setId("T001");
            teller.setThaiName("พนักงานธนาคาร");
            teller.setEnglishName("Bank Teller");
            teller.setPin(passwordEncoder.encode("1234"));
            teller.setRole("TELLER");

            userRepository.save(teller);
            System.out.println("Teller account created successfully");
        }

        // Seed Main Customer account
        if (!userRepository.existsById("customer@example.com")) {
            User customer = new User();
            customer.setEmail("customer@example.com");
            customer.setPassword(passwordEncoder.encode("password"));
            customer.setId("C001");
            customer.setThaiName("ลูกค้าทดสอบ");
            customer.setEnglishName("Test Customer");
            customer.setPin(passwordEncoder.encode("5678"));
            customer.setRole("CUSTOMER");

            userRepository.save(customer);
            System.out.println("Main customer account created successfully");
        }
    }
}