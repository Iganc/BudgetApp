package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("plainPassword123");
    }

    @Test
    void createUser_ShouldHashPassword() {
        when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = userService.createUser(testUser);

        assertNotEquals("plainPassword123", savedUser.getPassword(), "Password should be hashed");
        assertTrue(passwordEncoder.matches("plainPassword123", savedUser.getPassword()),
                   "Hashed password should match original");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_ShouldThrowException_WhenEmailExists() {
        when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(testUser));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginUser_ShouldSucceed_WithCorrectPassword() {
        String hashedPassword = passwordEncoder.encode("plainPassword123");
        testUser.setPassword(hashedPassword);
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        User loggedInUser = userService.loginUser("test@example.com", "plainPassword123");

        assertNotNull(loggedInUser);
        assertEquals(testUser.getEmail(), loggedInUser.getEmail());
    }

    @Test
    void loginUser_ShouldFail_WithWrongPassword() {
        String hashedPassword = passwordEncoder.encode("plainPassword123");
        testUser.setPassword(hashedPassword);
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class,
                     () -> userService.loginUser("test@example.com", "wrongPassword"));
    }

    @Test
    void loginUser_ShouldFail_WhenUserNotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                     () -> userService.loginUser("nonexistent@example.com", "anyPassword"));
    }

    @Test
    void changePassword_ShouldSucceed_WithCorrectOldPassword() {
        String hashedOldPassword = passwordEncoder.encode("oldPassword123");
        testUser.setPassword(hashedOldPassword);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.changePassword(1L, "oldPassword123", "newPassword456");

        verify(userRepository, times(1)).save(any(User.class));
        assertNotEquals(hashedOldPassword, testUser.getPassword(), "Password should be changed");
        assertTrue(passwordEncoder.matches("newPassword456", testUser.getPassword()),
                   "New password should match");
    }

    @Test
    void changePassword_ShouldFail_WithWrongOldPassword() {
        String hashedPassword = passwordEncoder.encode("oldPassword123");
        testUser.setPassword(hashedPassword);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class,
                     () -> userService.changePassword(1L, "wrongOldPassword", "newPassword456"));
        verify(userRepository, never()).save(any(User.class));
    }
}