package com.finbank.service;

import com.finbank.dto.AccountRequestDto;
import com.finbank.dto.AccountResponseDto;
import com.finbank.entity.Account;
import com.finbank.entity.AccountStatus;
import com.finbank.entity.AccountType;
import com.finbank.entity.Currency;
import com.finbank.entity.Customer;
import com.finbank.exception.CustomerNotFoundException;
import com.finbank.repository.AccountRepository;
import com.finbank.repository.CustomerRepository;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private CurrentUserService currentUserService;

    @InjectMocks private AccountService accountService;

    private Customer customer;
    private Account account;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("Aniket");
        customer.setLastName("Singh");
        customer.setEmail("aniket@example.com");

        account = new Account();
        account.setId(1L);
        account.setAccountNumber("FIN10000001");
        account.setBalance(new BigDecimal("10000.00"));
        account.setStatus(AccountStatus.ACTIVE);
        account.setCustomer(customer);
        account.setAccountType(AccountType.SAVINGS);
        account.setCurrency(Currency.INR);
    }

    @Test
    void createAccount_shouldCreateAccountForCurrentCustomer() {
        AccountRequestDto request = new AccountRequestDto();
        request.setAccountType(AccountType.SAVINGS);
        request.setCurrency(Currency.INR);

        when(currentUserService.getCurrentCustomerId()).thenReturn(1L);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AccountResponseDto result = accountService.createAccount(1L, request);

        assertNotNull(result);
        assertEquals(AccountType.SAVINGS, result.getAccountType());
        assertEquals(Currency.INR, result.getCurrency());
        assertNotNull(result.getAccountNumber());
        assertTrue(result.getAccountNumber().startsWith("FIN"));

        verify(customerRepository).findById(1L);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_shouldRejectDifferentCustomerId() {
        AccountRequestDto request = new AccountRequestDto();
        request.setAccountType(AccountType.SAVINGS);
        request.setCurrency(Currency.INR);

        when(currentUserService.getCurrentCustomerId()).thenReturn(1L);

        assertThrows(CustomerNotFoundException.class,
                () -> accountService.createAccount(2L, request));

        verifyNoInteractions(customerRepository);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void createAccount_shouldRejectUnknownCustomer() {
        AccountRequestDto request = new AccountRequestDto();
        request.setAccountType(AccountType.SAVINGS);
        request.setCurrency(Currency.INR);

        when(currentUserService.getCurrentCustomerId()).thenReturn(1L);
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> accountService.createAccount(1L, request));

        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void getAccount_shouldReturnAccountBelongingToCurrentCustomer() {
        when(currentUserService.getCurrentCustomerId()).thenReturn(1L);
        when(accountRepository.findByAccountNumberAndCustomerId("FIN10000001", 1L))
                .thenReturn(Optional.of(account));

        AccountResponseDto result = accountService.getAccountByAccountNumber("FIN10000001");

        assertNotNull(result);
        assertEquals("FIN10000001", result.getAccountNumber());
    }

    @Test
    void getAccount_shouldRejectAccountNotOwnedByCurrentCustomer() {
        when(currentUserService.getCurrentCustomerId()).thenReturn(1L);
        when(accountRepository.findByAccountNumberAndCustomerId("FIN99999999", 1L))
                .thenReturn(Optional.empty());

        assertThrows(com.finbank.exception.AccountNotFoundException.class,
                () -> accountService.getAccountByAccountNumber("FIN99999999"));
    }

    @Test
    void getCurrentCustomerAccounts_shouldReturnPagedAccounts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Account> page = new PageImpl<>(List.of(account));

        when(currentUserService.getCurrentCustomerId()).thenReturn(1L);
        when(accountRepository.findByCustomerId(1L, pageable)).thenReturn(page);

        Page<AccountResponseDto> result = accountService.getCurrentCustomerAccounts(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("FIN10000001", result.getContent().get(0).getAccountNumber());
    }

    @Test
    void getAllAccounts_shouldReturnPagedAccounts() {
        Pageable pageable = PageRequest.of(0, 10);
        when(accountRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(account)));

        Page<AccountResponseDto> result = accountService.getAllAccounts(pageable);

        assertEquals(1, result.getTotalElements());
        verify(accountRepository).findAll(pageable);
    }
}
