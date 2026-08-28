package com.finbank.service;

import com.finbank.entity.Customer;
import com.finbank.entity.User;
import com.finbank.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {

            throw new AuthenticationCredentialsNotFoundException(
                    "Authentication is required"
            );
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Authenticated user not found"
                        )
                );
    }

    public Customer getCurrentCustomer() {

        User user = getCurrentUser();

        if (user.getCustomer() == null) {
            throw new IllegalStateException(
                    "Authenticated user is not associated with a customer"
            );
        }

        return user.getCustomer();
    }

    public Long getCurrentCustomerId() {
        return getCurrentCustomer().getId();
    }

    public String getCurrentUserEmail() {
        return getCurrentUser().getEmail();
    }

    public boolean isCurrentCustomer(Long customerId) {
        return getCurrentCustomerId().equals(customerId);
    }
}