package com.example.demo.service;

import com.example.demo.model.Account;
import com.example.demo.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountNumberGenerator accountNumberGenerator;

    @Autowired
    public AccountService(AccountRepository accountRepository, AccountNumberGenerator accountNumberGenerator) {
        this.accountRepository = accountRepository;
        this.accountNumberGenerator = accountNumberGenerator;
    }

    public Account createAccount(String id, String thaiName, String englishName) {
        Account account = new Account(id, thaiName, englishName);
        account.setAccountNumber(accountNumberGenerator.generateAccountNumber());
        return accountRepository.save(account);
    }

    public Account getAccountByNumber(String accountNumber) {
        return accountRepository.findById(accountNumber)
                .orElseThrow(() -> new EntityNotFoundException("Account not found: " + accountNumber));
    }

    public Account getAccountByUserId(String userId) {
        return accountRepository.findByid(userId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found for user ID: " + userId));
    }
}
