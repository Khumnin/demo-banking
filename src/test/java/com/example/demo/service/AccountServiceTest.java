package com.example.demo.service;

import com.example.demo.model.Account;
import com.example.demo.repository.AccountRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountNumberGenerator accountNumberGenerator;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        accountService = new AccountService(accountRepository, accountNumberGenerator);
    }

    @Test
    void createAccount_ShouldCreateAndSaveAccount() {
        // Arrange
        String id = "123";
        String thaiName = "ทดสอบ";
        String englishName = "Test";
        String accountNumber = "1234567";
        when(accountNumberGenerator.generateAccountNumber()).thenReturn(accountNumber);
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Account result = accountService.createAccount(id, thaiName, englishName);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(thaiName, result.getThaiName());
        assertEquals(englishName, result.getEnglishName());
        assertEquals(accountNumber, result.getAccountNumber());
        verify(accountNumberGenerator).generateAccountNumber();
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void getAccountByNumber_WhenAccountExists_ReturnsAccount() {
        // Arrange
        String accountNumber = "1234567";
        Account expectedAccount = new Account();
        expectedAccount.setAccountNumber(accountNumber);
        when(accountRepository.findById(accountNumber)).thenReturn(Optional.of(expectedAccount));

        // Act
        Account result = accountService.getAccountByNumber(accountNumber);

        // Assert
        assertNotNull(result);
        assertEquals(accountNumber, result.getAccountNumber());
        verify(accountRepository).findById(accountNumber);
    }

    @Test
    void getAccountByNumber_WhenAccountDoesNotExist_ThrowsException() {
        // Arrange
        String accountNumber = "nonexistent";
        when(accountRepository.findById(accountNumber)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> accountService.getAccountByNumber(accountNumber));
        verify(accountRepository).findById(accountNumber);
    }

    @Test
    void getAccountByUserId_WhenAccountExists_ReturnsAccount() {
        // Arrange
        String userId = "123";
        Account expectedAccount = new Account();
        expectedAccount.setId(userId);
        when(accountRepository.findByid(userId)).thenReturn(Optional.of(expectedAccount));

        // Act
        Account result = accountService.getAccountByUserId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
        verify(accountRepository).findByid(userId);
    }

    @Test
    void getAccountByUserId_WhenAccountDoesNotExist_ThrowsException() {
        // Arrange
        String userId = "nonexistent";
        when(accountRepository.findByid(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> accountService.getAccountByUserId(userId));
        verify(accountRepository).findByid(userId);
    }
}