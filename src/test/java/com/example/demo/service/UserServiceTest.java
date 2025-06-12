package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository);
    }

    @Test
    void getUserIdByEmail_WhenUserExists_ReturnsUserId() {
        // Arrange
        String email = "test@example.com";
        String expectedId = "123";
        User user = new User();
        user.setEmail(email);
        user.setId(expectedId);
        when(userRepository.findById(email)).thenReturn(Optional.of(user));

        // Act
        String actualId = userService.getUserIdByEmail(email);

        // Assert
        assertEquals(expectedId, actualId);
        verify(userRepository).findById(email);
    }

    @Test
    void getUserIdByEmail_WhenUserDoesNotExist_ThrowsException() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findById(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> userService.getUserIdByEmail(email));
        verify(userRepository).findById(email);
    }
}