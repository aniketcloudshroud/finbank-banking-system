package com.finbank.service;

import com.finbank.dto.CustomerRequestDto;
import com.finbank.dto.CustomerResponseDto;
import com.finbank.dto.CustomerUpdateRequestDto;
import com.finbank.entity.Customer;
import com.finbank.entity.KycStatus;
import com.finbank.entity.Role;
import com.finbank.entity.User;
import com.finbank.exception.CustomerNotFoundException;
import com.finbank.exception.EmailAlreadyExistsException;
import com.finbank.repository.CustomerRepository;
import com.finbank.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;

    public CustomerService(
            CustomerRepository customerRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            CurrentUserService currentUserService
    ) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public CustomerResponseDto createCustomer(
            CustomerRequestDto request
    ) {

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(
                    "Email " + email + " is already registered"
            );
        }

        if (customerRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(
                    "Email " + email + " is already registered"
            );
        }

        Customer customer = new Customer();

        customer.setFirstName(request.getFirstName().trim());
        customer.setLastName(request.getLastName().trim());
        customer.setEmail(email);
        customer.setPhone(request.getPhone().trim());
        customer.setDateOfBirth(request.getDateOfBirth());

        /*
         * KYC is currently kept as an existing field in your
         * entity, but KYC functionality is not part of Phase 1.
         */
        customer.setKycStatus(KycStatus.PENDING);

        Customer savedCustomer =
                customerRepository.save(customer);

        User user = new User();

        user.setEmail(email);
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );
        user.setRole(Role.CUSTOMER);
        user.setCustomer(savedCustomer);

        userRepository.save(user);

        return new CustomerResponseDto(savedCustomer);
    }

    @Transactional
    public CustomerResponseDto getCurrentCustomer() {

        Customer customer =
                currentUserService.getCurrentCustomer();

        return new CustomerResponseDto(customer);
    }

    @Transactional
    public CustomerResponseDto getCustomerById(Long id) {

        Customer customer =
                customerRepository.findById(id)
                        .orElseThrow(() ->
                                new CustomerNotFoundException(
                                        "Customer with id " +
                                                id +
                                                " not found"
                                )
                        );

        return new CustomerResponseDto(customer);
    }

    public Page<CustomerResponseDto> getAllCustomers(
            Pageable pageable
    ) {

        return customerRepository
                .findAll(pageable)
                .map(CustomerResponseDto::new);
    }

    @Transactional
    public CustomerResponseDto updateCustomer(
            Long id,
            CustomerUpdateRequestDto requestDto
    ) {

        Customer customer =
                customerRepository.findById(id)
                        .orElseThrow(() ->
                                new CustomerNotFoundException(
                                        "Customer with id " +
                                                id +
                                                " not found"
                                )
                        );

        customer.setFirstName(
                requestDto.getFirstName().trim()
        );

        customer.setLastName(
                requestDto.getLastName().trim()
        );

        customer.setPhone(
                requestDto.getPhone().trim()
        );

        customer.setDateOfBirth(
                requestDto.getDateOfBirth()
        );

        Customer savedCustomer =
                customerRepository.save(customer);

        return new CustomerResponseDto(savedCustomer);
    }

    public boolean isCurrentCustomer(Long customerId) {
        return currentUserService
                .getCurrentCustomerId()
                .equals(customerId);
    }
}