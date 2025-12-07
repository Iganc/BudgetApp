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
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.mockito.Mockito.lenient;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private BCryptPasswordEncoder realEncoder;

    @BeforeEach
    void setUp() {
        realEncoder = new BCryptPasswordEncoder();

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("plainPassword123");
    }

    private void setupPasswordEncoder() {
        lenient().when(passwordEncoder.encode(anyString())).thenAnswer(invocation ->
                realEncoder.encode(invocation.getArgument(0)));
        lenient().when(passwordEncoder.matches(anyString(), anyString())).thenAnswer(invocation ->
                realEncoder.matches(invocation.getArgument(0), invocation.getArgument(1)));
    }

    @Test
    void createUser_ShouldHashPassword() {
        setupPasswordEncoder();
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
        setupPasswordEncoder();
        String hashedPassword = realEncoder.encode("plainPassword123");
        testUser.setPassword(hashedPassword);
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        User loggedInUser = userService.loginUser("test@example.com", "plainPassword123");

        assertNotNull(loggedInUser);
        assertEquals(testUser.getEmail(), loggedInUser.getEmail());
    }

    @Test
    void loginUser_ShouldFail_WithWrongPassword() {
        setupPasswordEncoder();
        String hashedPassword = realEncoder.encode("plainPassword123");
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
        setupPasswordEncoder();
        String hashedOldPassword = realEncoder.encode("oldPassword123");
        testUser.setPassword(hashedOldPassword);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.changePassword(1L, "oldPassword123", "newPassword456");

        verify(userRepository, times(1)).save(any(User.class));
        assertNotEquals(hashedOldPassword, testUser.getPassword(), "Password should be changed");
        assertTrue(realEncoder.matches("newPassword456", testUser.getPassword()),
                   "New password should match");
    }

    @Test
    void changePassword_ShouldFail_WithWrongOldPassword() {
        setupPasswordEncoder();
        String hashedPassword = realEncoder.encode("oldPassword123");
        testUser.setPassword(hashedPassword);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class,
                     () -> userService.changePassword(1L, "wrongOldPassword", "newPassword456"));
        verify(userRepository, never()).save(any(User.class));
    }
}