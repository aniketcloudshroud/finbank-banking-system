package com.finbank.service;

import com.finbank.entity.Customer;
import com.finbank.entity.Role;
import com.finbank.entity.User;
import com.finbank.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock private UserRepository userRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_shouldReturnAuthenticatedUser() {
        User user = buildUser();
        authenticate("aniket@example.com");
        when(userRepository.findByEmail("aniket@example.com")).thenReturn(Optional.of(user));

        CurrentUserService service = new CurrentUserService(userRepository);

        assertSame(user, service.getCurrentUser());
    }

    @Test
    void getCurrentUser_shouldRejectMissingAuthentication() {
        CurrentUserService service = new CurrentUserService(userRepository);

        assertThrows(AuthenticationCredentialsNotFoundException.class,
                service::getCurrentUser);

        verifyNoInteractions(userRepository);
    }

    @Test
    void getCurrentUser_shouldRejectUnknownAuthenticatedUser() {
        authenticate("unknown@example.com");
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        CurrentUserService service = new CurrentUserService(userRepository);

        assertThrows(org.springframework.security.core.userdetails.UsernameNotFoundException.class,
                service::getCurrentUser);
    }

    @Test
    void getCurrentCustomer_shouldReturnCustomer() {
        User user = buildUser();
        authenticate("aniket@example.com");
        when(userRepository.findByEmail("aniket@example.com")).thenReturn(Optional.of(user));

        CurrentUserService service = new CurrentUserService(userRepository);

        assertSame(user.getCustomer(), service.getCurrentCustomer());
        assertEquals(1L, service.getCurrentCustomerId());
        assertEquals("aniket@example.com", service.getCurrentUserEmail());
    }

    @Test
    void getCurrentCustomer_shouldRejectUserWithoutCustomer() {
        User user = buildUser();
        user.setCustomer(null);
        authenticate("aniket@example.com");
        when(userRepository.findByEmail("aniket@example.com")).thenReturn(Optional.of(user));

        CurrentUserService service = new CurrentUserService(userRepository);

        assertThrows(IllegalStateException.class, service::getCurrentCustomer);
    }

    @Test
    void isCurrentCustomer_shouldCompareAuthenticatedCustomerId() {
        User user = buildUser();
        authenticate("aniket@example.com");
        when(userRepository.findByEmail("aniket@example.com")).thenReturn(Optional.of(user));

        CurrentUserService service = new CurrentUserService(userRepository);

        assertTrue(service.isCurrentCustomer(1L));
        assertFalse(service.isCurrentCustomer(2L));
    }

    private void authenticate(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(email, "password", "ROLE_CUSTOMER")
        );
    }

    private User buildUser() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setEmail("aniket@example.com");

        User user = new User();
        user.setId(1L);
        user.setEmail("aniket@example.com");
        user.setPassword("encoded");
        user.setRole(Role.CUSTOMER);
        user.setEnabled(true);
        user.setCustomer(customer);
        return user;
    }
}
