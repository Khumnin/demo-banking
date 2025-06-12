package com.example.demo.service;

import com.example.demo.model.Account;
import com.example.demo.model.Transaction;
import com.example.demo.model.TransactionChannel;
import com.example.demo.model.User;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Optional;
import java.util.List;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        transactionService = new TransactionService(accountRepository, transactionRepository, userRepository);
    }

    @Test
    void deposit_WhenValid_ShouldCreateTransaction() {
        // Arrange
        String accountNumber = "1234567";
        double amount = 1000.0;
        String description = "Test deposit";
        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setBalance(0.0);
        when(accountRepository.findById(accountNumber)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArguments()[0]);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Transaction result = transactionService.deposit(accountNumber, amount, TransactionChannel.OTC, description);

        // Assert
        assertNotNull(result);
        assertEquals(accountNumber, result.getAccountNumber());
        assertEquals(amount, result.getAmount());
        assertEquals(amount, result.getBalance());
        assertEquals(TransactionChannel.OTC, result.getChannel());
        assertEquals(description, result.getDescription());
        verify(accountRepository).save(any(Account.class));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void deposit_WhenAccountNotFound_ShouldThrowException() {
        // Arrange
        String accountNumber = "nonexistent";
        when(accountRepository.findById(accountNumber)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> transactionService.deposit(accountNumber, 1000.0, TransactionChannel.OTC, "Test"));
    }

    @Test
    void deposit_WhenAmountInvalid_ShouldThrowException() {
        // Arrange
        String accountNumber = "1234567";
        Account account = new Account();
        account.setAccountNumber(accountNumber);
        when(accountRepository.findById(accountNumber)).thenReturn(Optional.of(account));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> transactionService.deposit(accountNumber, -1000.0, TransactionChannel.OTC, "Test"));
    }

    @Test
    void transfer_WhenValid_ShouldCreateTransactions() {
        // Arrange
        String sourceAccountNumber = "1234567";
        String destAccountNumber = "7654321";
        double amount = 1000.0;
        String pin = "1234";

        Account sourceAccount = new Account();
        sourceAccount.setAccountNumber(sourceAccountNumber);
        sourceAccount.setBalance(2000.0);
        sourceAccount.setId("user1");

        Account destAccount = new Account();
        destAccount.setAccountNumber(destAccountNumber);
        destAccount.setBalance(0.0);

        User user = new User();
        user.setPin("{noop}1234"); // Changed from encoded PIN to {noop} prefix

        when(accountRepository.findById(sourceAccountNumber)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(destAccountNumber)).thenReturn(Optional.of(destAccount));
        when(userRepository.findByid("user1")).thenReturn(Optional.of(user));
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArguments()[0]);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Transaction result = transactionService.transfer(sourceAccountNumber, destAccountNumber, amount, pin);

        // Assert
        assertNotNull(result);
        assertEquals(destAccountNumber, result.getAccountNumber());
        assertEquals(amount, result.getAmount());
        assertEquals(amount, result.getBalance());
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    void transfer_WhenInsufficientBalance_ShouldThrowException() {
        // Arrange
        String sourceAccountNumber = "1234567";
        String destAccountNumber = "7654321";
        double amount = 2000.0;

        Account sourceAccount = new Account();
        sourceAccount.setAccountNumber(sourceAccountNumber);
        sourceAccount.setBalance(1000.0);

        Account destAccount = new Account();
        destAccount.setAccountNumber(destAccountNumber);

        when(accountRepository.findById(sourceAccountNumber)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(destAccountNumber)).thenReturn(Optional.of(destAccount));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> transactionService.transfer(sourceAccountNumber, destAccountNumber, amount, "1234"));
    }

    @Test
    void transfer_WhenDestinationAccountNotFound_ShouldThrowException() {
        // Arrange
        String sourceAccountNumber = "1234567";
        String destAccountNumber = "nonexistent";
        double amount = 1000.0;

        Account sourceAccount = new Account();
        sourceAccount.setAccountNumber(sourceAccountNumber);
        sourceAccount.setBalance(2000.0);
        sourceAccount.setId("user1");

        User user = new User();
        user.setPin("$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG"); // encoded "1234"

        when(accountRepository.findById(sourceAccountNumber)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(destAccountNumber)).thenReturn(Optional.empty());
        when(userRepository.findByid("user1")).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> transactionService.transfer(sourceAccountNumber, destAccountNumber, amount, "1234"));
        verify(accountRepository).findById(sourceAccountNumber);
        verify(accountRepository).findById(destAccountNumber);
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void getTransactionsByAccountNumber_ShouldReturnTransactions() {
        // Arrange
        String accountNumber = "1234567";
        List<Transaction> expectedTransactions = Arrays.asList(
                new Transaction(accountNumber, 1000.0, 1000.0, TransactionChannel.OTC, "Test"),
                new Transaction(accountNumber, -500.0, 500.0, TransactionChannel.OTC, "Test"));
        when(transactionRepository.findByAccountNumberOrderByDateAscTimeAsc(accountNumber))
                .thenReturn(expectedTransactions);

        // Act
        List<Transaction> result = transactionService.getTransactionsByAccountNumber(accountNumber);

        // Assert
        assertNotNull(result);
        assertEquals(expectedTransactions.size(), result.size());
        verify(transactionRepository).findByAccountNumberOrderByDateAscTimeAsc(accountNumber);
    }
}