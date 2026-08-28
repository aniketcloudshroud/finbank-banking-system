package com.finbank.ai;

import com.finbank.dto.AccountResponseDto;
import com.finbank.dto.TransactionResponseDto;
import com.finbank.entity.Account;
import com.finbank.entity.Customer;
import com.finbank.entity.Transaction;
import com.finbank.repository.AccountRepository;
import com.finbank.repository.TransactionRepository;
import com.finbank.service.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialAssistantToolsTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CurrentUserService currentUserService;

    private FinancialAssistantTools tools;

    private Customer customer;
    private Account account;

    @BeforeEach
    void setUp() {

        tools = new FinancialAssistantTools(
                accountRepository,
                transactionRepository,
                currentUserService
        );

        customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("Test");
        customer.setLastName("Customer");
        customer.setEmail("test@example.com");

        account = new Account();
        account.setId(100L);
        account.setAccountNumber("TEST-ACCOUNT-001");
        account.setBalance(new BigDecimal("10000.00"));
        account.setCustomer(customer);
    }

    // ---------------------------------------------------------
    // getMyAccounts()
    // ---------------------------------------------------------

    @Test
    void getMyAccounts_shouldReturnCurrentCustomersAccounts() {

        when(currentUserService.getCurrentCustomerId())
                .thenReturn(1L);

        when(accountRepository.findByCustomerId(
                eq(1L),
                any(PageRequest.class)
        )).thenReturn(
                new PageImpl<>(List.of(account))
        );

        List<AccountResponseDto> result =
                tools.getMyAccounts();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(
                "TEST-ACCOUNT-001",
                result.get(0).getAccountNumber()
        );

        verify(currentUserService)
                .getCurrentCustomerId();

        verify(accountRepository)
                .findByCustomerId(
                        eq(1L),
                        any(PageRequest.class)
                );
    }

    @Test
    void getMyAccounts_shouldReturnEmptyListWhenCustomerHasNoAccounts() {

        when(currentUserService.getCurrentCustomerId())
                .thenReturn(1L);

        when(accountRepository.findByCustomerId(
                eq(1L),
                any(PageRequest.class)
        )).thenReturn(
                new PageImpl<>(List.of())
        );

        List<AccountResponseDto> result =
                tools.getMyAccounts();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(accountRepository)
                .findByCustomerId(
                        eq(1L),
                        any(PageRequest.class)
                );
    }

    // ---------------------------------------------------------
    // getMyRecentTransactions()
    // ---------------------------------------------------------

    @Test
    void getMyRecentTransactions_shouldReturnTransactions() {

        when(currentUserService.getCurrentCustomerId())
                .thenReturn(1L);

        Transaction transaction = mock(Transaction.class);

        when(transactionRepository
                .findBySourceAccountCustomerIdOrDestinationAccountCustomerId(
                        eq(1L),
                        eq(1L),
                        any(PageRequest.class)
                ))
                .thenReturn(
                        new PageImpl<>(List.of(transaction))
                );

        List<TransactionResponseDto> result =
                tools.getMyRecentTransactions(10);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(currentUserService)
                .getCurrentCustomerId();

        verify(transactionRepository)
                .findBySourceAccountCustomerIdOrDestinationAccountCustomerId(
                        eq(1L),
                        eq(1L),
                        any(PageRequest.class)
                );
    }

    @Test
    void getMyRecentTransactions_shouldClampLimitTo50() {

        when(currentUserService.getCurrentCustomerId())
                .thenReturn(1L);

        when(transactionRepository
                .findBySourceAccountCustomerIdOrDestinationAccountCustomerId(
                        eq(1L),
                        eq(1L),
                        any(PageRequest.class)
                ))
                .thenReturn(
                        new PageImpl<>(List.of())
                );

        tools.getMyRecentTransactions(1000);

        verify(transactionRepository)
                .findBySourceAccountCustomerIdOrDestinationAccountCustomerId(
                        eq(1L),
                        eq(1L),
                        argThat(pageable ->
                                pageable.getPageSize() == 50
                        )
                );
    }

    @Test
    void getMyRecentTransactions_shouldClampLimitTo1() {

        when(currentUserService.getCurrentCustomerId())
                .thenReturn(1L);

        when(transactionRepository
                .findBySourceAccountCustomerIdOrDestinationAccountCustomerId(
                        eq(1L),
                        eq(1L),
                        any(PageRequest.class)
                ))
                .thenReturn(
                        new PageImpl<>(List.of())
                );

        tools.getMyRecentTransactions(0);

        verify(transactionRepository)
                .findBySourceAccountCustomerIdOrDestinationAccountCustomerId(
                        eq(1L),
                        eq(1L),
                        argThat(pageable ->
                                pageable.getPageSize() == 1
                        )
                );
    }

    // ---------------------------------------------------------
    // getMyAccount()
    // ---------------------------------------------------------

    @Test
    void getMyAccount_shouldReturnOwnedAccount() {

        when(currentUserService.getCurrentCustomerId())
                .thenReturn(1L);

        when(accountRepository
                .findByAccountNumberAndCustomerId(
                        "TEST-ACCOUNT-001",
                        1L
                ))
                .thenReturn(Optional.of(account));

        AccountResponseDto result =
                tools.getMyAccount("TEST-ACCOUNT-001");

        assertNotNull(result);
        assertEquals(
                "TEST-ACCOUNT-001",
                result.getAccountNumber()
        );

        verify(accountRepository)
                .findByAccountNumberAndCustomerId(
                        "TEST-ACCOUNT-001",
                        1L
                );
    }

    @Test
    void getMyAccount_shouldRejectAccountNotOwnedByCustomer() {

        when(currentUserService.getCurrentCustomerId())
                .thenReturn(1L);

        when(accountRepository
                .findByAccountNumberAndCustomerId(
                        "OTHER-ACCOUNT",
                        1L
                ))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> tools.getMyAccount("OTHER-ACCOUNT")
        );

        verify(accountRepository)
                .findByAccountNumberAndCustomerId(
                        "OTHER-ACCOUNT",
                        1L
                );
    }

    // ---------------------------------------------------------
    // getMyTransaction()
    // ---------------------------------------------------------

    @Test
    void getMyTransaction_shouldReturnSourceTransaction() {

        when(currentUserService.getCurrentCustomerId())
                .thenReturn(1L);

        Transaction transaction =
                mock(Transaction.class);

        when(transactionRepository.findByReference(
                "TEST-TXN-001"
        )).thenReturn(Optional.of(transaction));

        Account sourceAccount =
                mock(Account.class);

        when(transaction.getSourceAccount())
                .thenReturn(sourceAccount);

        when(transaction.getDestinationAccount())
                .thenReturn(null);

        when(sourceAccount.getCustomer())
                .thenReturn(customer);

        TransactionResponseDto result =
                tools.getMyTransaction("TEST-TXN-001");

        assertNotNull(result);

        verify(transactionRepository)
                .findByReference("TEST-TXN-001");
    }

    @Test
    void getMyTransaction_shouldReturnDestinationTransaction() {

        when(currentUserService.getCurrentCustomerId())
                .thenReturn(1L);

        Transaction transaction =
                mock(Transaction.class);

        when(transactionRepository.findByReference(
                "TEST-TXN-002"
        )).thenReturn(Optional.of(transaction));

        when(transaction.getSourceAccount())
                .thenReturn(null);

        Account destinationAccount =
                mock(Account.class);

        when(transaction.getDestinationAccount())
                .thenReturn(destinationAccount);

        when(destinationAccount.getCustomer())
                .thenReturn(customer);

        TransactionResponseDto result =
                tools.getMyTransaction("TEST-TXN-002");

        assertNotNull(result);

        verify(transactionRepository)
                .findByReference("TEST-TXN-002");
    }

    @Test
    void getMyTransaction_shouldRejectUnknownTransaction() {

        when(currentUserService.getCurrentCustomerId())
                .thenReturn(1L);

        when(transactionRepository.findByReference(
                "UNKNOWN-TXN"
        )).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> tools.getMyTransaction("UNKNOWN-TXN")
        );

        verify(transactionRepository)
                .findByReference("UNKNOWN-TXN");
    }

    @Test
    void getMyTransaction_shouldRejectTransactionBelongingToAnotherCustomer() {

        when(currentUserService.getCurrentCustomerId())
                .thenReturn(1L);

        Transaction transaction =
                mock(Transaction.class);

        when(transactionRepository.findByReference(
                "OTHER-TXN"
        )).thenReturn(Optional.of(transaction));

        Customer otherCustomer = new Customer();
        otherCustomer.setId(2L);

        Account sourceAccount =
                mock(Account.class);

        when(transaction.getSourceAccount())
                .thenReturn(sourceAccount);

        when(sourceAccount.getCustomer())
                .thenReturn(otherCustomer);

        when(transaction.getDestinationAccount())
                .thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> tools.getMyTransaction("OTHER-TXN")
        );
    }
}