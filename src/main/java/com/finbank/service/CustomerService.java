package com.finbank.service;

import com.finbank.dto.*;
import com.finbank.entity.*;
import com.finbank.exception.*;
import com.finbank.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(CustomerRepository customerRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {

        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public CustomerResponseDto createCustomer(CustomerRequestDto request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "Email " + request.getEmail() + " is already registered"
            );
        }

        Customer customer = new Customer();

        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setKycStatus(KycStatus.PENDING);

        Customer savedCustomer = customerRepository.save(customer);

        User user = new User();

        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CUSTOMER);
        user.setCustomer(savedCustomer);

        userRepository.save(user);

        return new CustomerResponseDto(savedCustomer);
    }


    public CustomerResponseDto getCustomerById(Long id) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() ->
                        new CustomerNotFoundException(
                                "Customer with id " + id + " not found"
                        )
                );

        return new CustomerResponseDto(customer);
    }


    public List<CustomerResponseDto> getAllCustomers() {

        return customerRepository.findAll()
                .stream()
                .map(CustomerResponseDto::new)
                .toList();
    }


    public Page<CustomerResponseDto> getAllCustomers(Pageable pageable) {

        return customerRepository.findAll(pageable)
                .map(CustomerResponseDto::new);
    }


    public CustomerResponseDto updateCustomer(
            Long id,
            CustomerUpdateRequestDto requestDto) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() ->
                        new CustomerNotFoundException(
                                "Customer with id " + id + " not found"
                        )
                );

        customer.setFirstName(requestDto.getFirstName());
        customer.setLastName(requestDto.getLastName());
        customer.setPhone(requestDto.getPhone());
        customer.setDateOfBirth(requestDto.getDateOfBirth());

        Customer savedCustomer = customerRepository.save(customer);

        return new CustomerResponseDto(savedCustomer);
    }


    public CustomerResponseDto updateKycStatus(
            Long customerId,
            KycStatus status) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new CustomerNotFoundException(
                                "Customer with id " + customerId + " not found"
                        )
                );

        customer.setKycStatus(status);

        Customer savedCustomer = customerRepository.save(customer);

        return new CustomerResponseDto(savedCustomer);
    }


    private Long getAuthenticatedCustomerId() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User with email " + email + " not found"
                        )
                );

        return user.getCustomer().getId();
    }


    public boolean isCurrentCustomer(Long customerId) {

        return getAuthenticatedCustomerId().equals(customerId);
    }
}