package com.example.demo.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.repository.UserRepository;

@Component
public class ServiceHealthIndicator implements HealthIndicator {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Health health() {
        try {
            // Check if repositories are accessible
            accountRepository.count();
            transactionRepository.count();
            userRepository.count();

            return Health.up()
                    .withDetail("services", "All services are running")
                    .withDetail("accountService", "UP")
                    .withDetail("transactionService", "UP")
                    .withDetail("userService", "UP")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("services", "One or more services are down")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}