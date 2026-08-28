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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock private CustomerRepository customerRepository;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private CurrentUserService currentUserService;

    @InjectMocks private CustomerService customerService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("Aniket");
        customer.setLastName("Singh");
        customer.setEmail("aniket@example.com");
        customer.setPhone("9876543210");
        customer.setDateOfBirth(LocalDate.of(2000, 1, 1));
        customer.setKycStatus(KycStatus.PENDING);
    }

    @Test
    void createCustomer_shouldCreateCustomerAndUser() {
        CustomerRequestDto request = new CustomerRequestDto();
        request.setFirstName(" Aniket ");
        request.setLastName(" Singh ");
        request.setEmail(" ANIKET@EXAMPLE.COM ");
        request.setPhone(" 9876543210 ");
        request.setDateOfBirth(LocalDate.of(2000, 1, 1));
        request.setPassword("password123");

        when(userRepository.existsByEmail("aniket@example.com")).thenReturn(false);
        when(customerRepository.existsByEmail("aniket@example.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerResponseDto result = customerService.createCustomer(request);

        assertNotNull(result);
        assertEquals("aniket@example.com", result.getEmail());
        assertEquals("Aniket", result.getFirstName());
        assertEquals(KycStatus.PENDING, result.getKycStatus());

        verify(customerRepository).save(any(Customer.class));
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(argThat(user ->
                user.getEmail().equals("aniket@example.com") &&
                        user.getRole() == Role.CUSTOMER &&
                        user.getCustomer() != null));
    }

    @Test
    void createCustomer_shouldRejectExistingUserEmail() {
        CustomerRequestDto request = validCreateRequest();
        when(userRepository.existsByEmail("aniket@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,
                () -> customerService.createCustomer(request));

        verify(customerRepository, never()).save(any(Customer.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createCustomer_shouldRejectExistingCustomerEmail() {
        CustomerRequestDto request = validCreateRequest();
        when(userRepository.existsByEmail("aniket@example.com")).thenReturn(false);
        when(customerRepository.existsByEmail("aniket@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,
                () -> customerService.createCustomer(request));

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void getCurrentCustomer_shouldReturnAuthenticatedCustomer() {
        when(currentUserService.getCurrentCustomer()).thenReturn(customer);

        CustomerResponseDto result = customerService.getCurrentCustomer();

        assertEquals(1L, result.getId());
        assertEquals("aniket@example.com", result.getEmail());
    }

    @Test
    void getCustomerById_shouldReturnCustomer() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        CustomerResponseDto result = customerService.getCustomerById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void getCustomerById_shouldThrowWhenMissing() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> customerService.getCustomerById(999L));
    }

    @Test
    void getAllCustomers_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(customerRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(customer)));

        Page<CustomerResponseDto> result = customerService.getAllCustomers(pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void updateCustomer_shouldUpdateAllowedFields() {
        CustomerUpdateRequestDto request = new CustomerUpdateRequestDto();
        request.setFirstName(" NewFirst ");
        request.setLastName(" NewLast ");
        request.setPhone("9999999999");
        request.setDateOfBirth(LocalDate.of(2001, 2, 2));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(customer)).thenReturn(customer);

        CustomerResponseDto result = customerService.updateCustomer(1L, request);

        assertEquals("NewFirst", result.getFirstName());
        assertEquals("NewLast", result.getLastName());
        assertEquals("9999999999", result.getPhone());
        assertEquals(LocalDate.of(2001, 2, 2), result.getDateOfBirth());
        assertEquals("aniket@example.com", result.getEmail());
        verify(customerRepository).save(customer);
    }

    @Test
    void updateCustomer_shouldThrowWhenMissing() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> customerService.updateCustomer(999L, new CustomerUpdateRequestDto()));
    }

    @Test
    void isCurrentCustomer_shouldDelegateToCurrentUserService() {
        when(currentUserService.getCurrentCustomerId()).thenReturn(1L);

        assertTrue(customerService.isCurrentCustomer(1L));
        assertFalse(customerService.isCurrentCustomer(2L));
    }

    private CustomerRequestDto validCreateRequest() {
        CustomerRequestDto request = new CustomerRequestDto();
        request.setFirstName("Aniket");
        request.setLastName("Singh");
        request.setEmail("aniket@example.com");
        request.setPhone("9876543210");
        request.setDateOfBirth(LocalDate.of(2000, 1, 1));
        request.setPassword("password123");
        return request;
    }
}
