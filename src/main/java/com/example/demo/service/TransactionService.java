package com.example.demo.service;

import com.example.demo.model.Account;
import com.example.demo.model.Transaction;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import com.example.demo.model.TransactionChannel;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import java.util.List;

@Service
public class TransactionService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Autowired
    public TransactionService(AccountRepository accountRepository, TransactionRepository transactionRepository,
            UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Transaction deposit(String accountNumber, double amount, TransactionChannel channel, String description) {
        // Find the account
        Account account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new EntityNotFoundException("Account not found: " + accountNumber));

        // Validate amount
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero");
        }

        // Update account balance
        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);

        // Create and save transaction record
        Transaction transaction = new Transaction(accountNumber, amount, account.getBalance(), channel, description);
        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction transfer(String sourceAccountNumber, String destinationAccountNumber, double amount,
            String pin) {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        // Find the accounts
        Account sourceAccount = accountRepository.findById(sourceAccountNumber)
                .orElseThrow(() -> new EntityNotFoundException("Source account not found: " + sourceAccountNumber));

        Account destinationAccount = accountRepository.findById(destinationAccountNumber)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Destination account not found: " + destinationAccountNumber));

        // Validate amount
        if (amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero");
        }

        // Validate source account has sufficient balance
        if (sourceAccount.getBalance() < amount) {
            throw new IllegalArgumentException("Insufficient balance for transfer");
        }

        // Validate PIN
        User user = userRepository.findByid(sourceAccount.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found for account: " + sourceAccountNumber));

        if (!encoder.matches(pin, user.getPin())) {
            throw new IllegalArgumentException("Invalid PIN");
        }

        // Update account balances
        sourceAccount.setBalance(sourceAccount.getBalance() - amount);
        destinationAccount.setBalance(destinationAccount.getBalance() + amount);

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        // Create and save transaction record
        // Source account transaction
        Transaction transaction = new Transaction(sourceAccountNumber, destinationAccountNumber, amount * -1,
                sourceAccount.getBalance(), TransactionChannel.OTC, "Transfer via OTC");
        transactionRepository.save(transaction);
        // Destination account transaction
        transaction = new Transaction(destinationAccountNumber, sourceAccountNumber, amount,
                destinationAccount.getBalance(), TransactionChannel.ATS, "Transfer via ATS");
        transaction.setDescription(String.format("Transfer from %s: %s", sourceAccountNumber, "Transfer via ATS"));
        transactionRepository.save(transaction);

        return transaction;
    }

    public List<Transaction> getTransactionsByAccountNumber(String accountNumber) {
        return transactionRepository.findByAccountNumberOrderByDateAscTimeAsc(accountNumber);
    }
}
