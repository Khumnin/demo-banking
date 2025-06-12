package com.example.demo.service;

import org.springframework.stereotype.Service;
import com.example.demo.repository.AccountRepository;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AccountNumberGenerator {
    private static final Logger logger = LoggerFactory.getLogger(AccountNumberGenerator.class);
    private static final int MAX_RETRIES = 10;
    private static final int ACCOUNT_NUMBER_LENGTH = 7;

    private final AccountRepository accountRepository;
    private final Random random;

    public AccountNumberGenerator(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
        this.random = new Random();
    }

    public String generateAccountNumber() {
        int retryCount = 0;
        String accountNumber;

        do {
            if (retryCount >= MAX_RETRIES) {
                logger.error("Failed to generate unique account number after {} attempts", MAX_RETRIES);
                throw new RuntimeException("Unable to generate unique account number. Please try again.");
            }

            // Generate a 7-digit number
            accountNumber = String.format("%0" + ACCOUNT_NUMBER_LENGTH + "d",
                    random.nextInt((int) Math.pow(10, ACCOUNT_NUMBER_LENGTH)));
            logger.debug("Generated account number attempt {}: {}", retryCount + 1, accountNumber);

            retryCount++;
        } while (accountRepository.existsById(accountNumber));

        logger.info("Successfully generated unique account number: {} after {} attempts", accountNumber, retryCount);
        return accountNumber;
    }
}