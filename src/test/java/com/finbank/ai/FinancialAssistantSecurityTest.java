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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialAssistantSecurityTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CurrentUserService currentUserService;

    private FinancialAssistantTools tools;

    private Customer currentCustomer;
    private Customer anotherCustomer;

    @BeforeEach
    void setUp() {

        tools = new FinancialAssistantTools(
                accountRepository,
                transactionRepository,
                currentUserService
        );

        currentCustomer = new Customer();
        currentCustomer.setId(1L);

        anotherCustomer = new Customer();
        anotherCustomer.setId(2L);
    }

    @Test
    void getMyAccounts_shouldOnlyUseAuthenticatedCustomerId() {

        when(currentUserService.getCurrentCustomerId())
                .thenReturn(1L);

        when(accountRepository.findByCustomerId(
                eq(1L),
                any(PageRequest.class)
        )).thenReturn(
                new PageImpl<>(List.of())
        );

        tools.getMyAccounts();

        verify(accountRepository)
                .findByCustomerId(
                        eq(1L),
                        any(PageRequest.class)
                );

        verify(accountRepository, never())
                .findByCustomerId(
                        eq(2L),
                        any(PageRequest.class)
                );
    }

    @Test
    void getMyRecentTransactions_shouldOnlyUseAuthenticatedCustomerId() {

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

        tools.getMyRecentTransactions(10);

        verify(transactionRepository)
                .findBySourceAccountCustomerIdOrDestinationAccountCustomerId(
                        eq(1L),
                        eq(1L),
                        any(PageRequest.class)
                );

        verify(transactionRepository, never())
                .findBySourceAccountCustomerIdOrDestinationAccountCustomerId(
                        eq(2L),
                        eq(2L),
                        any(PageRequest.class)
                );
    }

    @Test
    void getMyAccount_shouldRejectAnotherCustomersAccount() {

        when(currentUserService.getCurrentCustomerId())
                .thenReturn(1L);

        when(accountRepository.findByAccountNumberAndCustomerId(
                "ACC-OTHER",
                1L
        )).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> tools.getMyAccount("ACC-OTHER")
        );

        verify(accountRepository)
                .findByAccountNumberAndCustomerId(
                        "ACC-OTHER",
                        1L
                );
    }

    @Test
    void getMyTransaction_shouldRejectAnotherCustomersTransaction() {

        when(currentUserService.getCurrentCustomerId())
                .thenReturn(1L);

        Transaction transaction =
                mock(Transaction.class);

        Account sourceAccount =
                mock(Account.class);

        when(transactionRepository.findByReference(
                "OTHER-TXN"
        )).thenReturn(Optional.of(transaction));

        when(transaction.getSourceAccount())
                .thenReturn(sourceAccount);

        when(transaction.getDestinationAccount())
                .thenReturn(null);

        when(sourceAccount.getCustomer())
                .thenReturn(anotherCustomer);

        assertThrows(
                IllegalArgumentException.class,
                () -> tools.getMyTransaction("OTHER-TXN")
        );
    }

    @Test
    void getMyTransaction_shouldAllowCurrentCustomersTransaction() {

        when(currentUserService.getCurrentCustomerId())
                .thenReturn(1L);

        Transaction transaction =
                mock(Transaction.class);

        Account sourceAccount =
                mock(Account.class);

        when(transactionRepository.findByReference(
                "MY-TXN"
        )).thenReturn(Optional.of(transaction));

        when(transaction.getSourceAccount())
                .thenReturn(sourceAccount);

        when(transaction.getDestinationAccount())
                .thenReturn(null);

        when(sourceAccount.getCustomer())
                .thenReturn(currentCustomer);

        TransactionResponseDto result =
                tools.getMyTransaction("MY-TXN");

        assertNotNull(result);

        verify(transactionRepository)
                .findByReference("MY-TXN");
    }

    @Test
    void getMyAccounts_shouldNotTrustClientProvidedCustomerId() {

        when(currentUserService.getCurrentCustomerId())
                .thenReturn(1L);

        when(accountRepository.findByCustomerId(
                eq(1L),
                any(PageRequest.class)
        )).thenReturn(
                new PageImpl<>(List.of())
        );

        // There is deliberately no customerId parameter.
        // The tool must derive the identity from CurrentUserService.
        tools.getMyAccounts();

        verify(currentUserService)
                .getCurrentCustomerId();

        verify(accountRepository)
                .findByCustomerId(
                        eq(1L),
                        any(PageRequest.class)
                );
    }

    @Test
    void getMyRecentTransactions_shouldClampLimitToMaximum50() {

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

        tools.getMyRecentTransactions(5000);

        verify(transactionRepository)
                .findBySourceAccountCustomerIdOrDestinationAccountCustomerId(
                        eq(1L),
                        eq(1L),
                        argThat(page ->
                                page.getPageSize() == 50
                        )
                );
    }

    @Test
    void getMyRecentTransactions_shouldClampLimitToMinimum1() {

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

        tools.getMyRecentTransactions(-100);

        verify(transactionRepository)
                .findBySourceAccountCustomerIdOrDestinationAccountCustomerId(
                        eq(1L),
                        eq(1L),
                        argThat(page ->
                                page.getPageSize() == 1
                        )
                );
    }
}