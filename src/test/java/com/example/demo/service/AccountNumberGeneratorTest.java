package com.example.demo.service;

import com.example.demo.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountNumberGeneratorTest {

    @Mock
    private AccountRepository accountRepository;

    private AccountNumberGenerator accountNumberGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        accountNumberGenerator = new AccountNumberGenerator(accountRepository);
    }

    @Test
    void generateAccountNumber_ShouldGenerateUniqueNumber() {
        // Arrange
        String generatedNumber = "1234567";
        when(accountRepository.existsById(anyString())).thenReturn(false);

        // Act
        String result = accountNumberGenerator.generateAccountNumber();

        // Assert
        assertNotNull(result);
        assertEquals(7, result.length());
        assertTrue(result.matches("\\d{7}"));
        verify(accountRepository).existsById(result);
    }

    @Test
    void generateAccountNumber_WhenNumberExists_ShouldGenerateNewNumber() {
        // Arrange
        when(accountRepository.existsById("1234567")).thenReturn(true);
        when(accountRepository.existsById("7654321")).thenReturn(false);

        // Act
        String result = accountNumberGenerator.generateAccountNumber();

        // Assert
        assertNotNull(result);
        assertEquals(7, result.length());
        assertTrue(result.matches("\\d{7}"));
        verify(accountRepository, atLeastOnce()).existsById(anyString());
    }

    @Test
    void generateAccountNumber_WhenMaxRetriesExceeded_ShouldThrowException() {
        // Arrange
        when(accountRepository.existsById(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> accountNumberGenerator.generateAccountNumber());
        verify(accountRepository, times(10)).existsById(anyString());
    }
}