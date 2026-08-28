package com.finbank.service;

import com.finbank.dto.*;
import com.finbank.entity.Account;
import com.finbank.entity.AccountStatus;
import com.finbank.entity.Customer;
import com.finbank.entity.Transaction;
import com.finbank.entity.TransactionStatus;
import com.finbank.entity.TransactionType;
import com.finbank.exception.*;
import com.finbank.repository.AccountRepository;
import com.finbank.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private TransactionService transactionService;

    private Customer customer;

    private Account account;
    Account destinationAccount;

    @BeforeEach
    void setUp() {

        customer = new Customer();
        customer.setId(1L);

        account = new Account();
        account.setId(1L);
        account.setAccountNumber("FIN10000001");
        account.setBalance(new BigDecimal("10000.00"));
        account.setStatus(AccountStatus.ACTIVE);
        account.setCustomer(customer);

        Customer otherCustomer = new Customer();
        otherCustomer.setId(999L);

        Account otherAccount = new Account();
        otherAccount.setId(99L);
        otherAccount.setAccountNumber("FIN99999999");
        otherAccount.setCustomer(otherCustomer);

        destinationAccount = new Account();
        destinationAccount.setId(2L);
        destinationAccount.setAccountNumber("FIN10000002");
        destinationAccount.setBalance(new BigDecimal("5000.00"));
        destinationAccount.setStatus(AccountStatus.ACTIVE);

        Customer destinationCustomer = new Customer();
        destinationCustomer.setId(2L);

        destinationAccount.setCustomer(destinationCustomer);
    }

    @Test
    void deposit_shouldIncreaseBalanceAndCreateTransaction() {

        Long customerId = 1L;
        BigDecimal depositAmount = new BigDecimal("500.00");

        DepositRequestDto request = new DepositRequestDto();
        request.setAmount(depositAmount);
        request.setDescription("Test deposit");

        when(currentUserService.getCurrentCustomerId())
                .thenReturn(customerId);

        when(accountRepository
                .findByAccountNumberAndCustomerIdForUpdate(
                        "FIN10000001",
                        customerId
                ))
                .thenReturn(Optional.of(account));

        when(transactionRepository.existsByReference(anyString()))
                .thenReturn(false);

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponseDto result =
                transactionService.deposit(
                        "FIN10000001",
                        request
                );

        assertEquals(
                new BigDecimal("10500.00"),
                account.getBalance()
        );

        assertNotNull(result);

        verify(accountRepository)
                .save(account);

        verify(transactionRepository)
                .save(any(Transaction.class));
    }

    @Test
    void deposit_shouldThrowException_whenAccountDoesNotExist() {

        when(currentUserService.getCurrentCustomerId())
                .thenReturn(1L);

        when(accountRepository
                .findByAccountNumberAndCustomerIdForUpdate(
                        "INVALID",
                        1L
                ))
                .thenReturn(Optional.empty());

        DepositRequestDto request = new DepositRequestDto();
        request.setAmount(new BigDecimal("500.00"));
        request.setDescription("Test deposit");

        assertThrows(
                AccountNotFoundException.class,
                () -> transactionService.deposit(
                        "INVALID",
                        request
                )
        );

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void deposit_shouldThrowException_whenAccountIsInactive() {

        account.setStatus(AccountStatus.BLOCKED);

        when(currentUserService.getCurrentCustomerId())
                .thenReturn(1L);

        when(accountRepository
                .findByAccountNumberAndCustomerIdForUpdate(
                        "FIN10000001",
                        1L
                ))
                .thenReturn(Optional.of(account));

        DepositRequestDto request = new DepositRequestDto();
        request.setAmount(new BigDecimal("500.00"));

        assertThrows(
                AccountNotActiveException.class,
                () -> transactionService.deposit(
                        "FIN10000001",
                        request
                )
        );

        assertEquals(
                new BigDecimal("10000.00"),
                account.getBalance()
        );
    }

    @Test
    void withdraw_shouldDecreaseBalance() {

        Long customerId = 1L;

        WithdrawalRequestDto request =
                new WithdrawalRequestDto();

        request.setAmount(
                new BigDecimal("2500.00")
        );

        request.setDescription("ATM withdrawal");

        when(currentUserService.getCurrentCustomerId())
                .thenReturn(customerId);

        when(accountRepository
                .findByAccountNumberAndCustomerIdForUpdate(
                        "FIN10000001",
                        customerId
                ))
                .thenReturn(Optional.of(account));

        when(transactionRepository.existsByReference(anyString()))
                .thenReturn(false);

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponseDto result =
                transactionService.withdraw(
                        "FIN10000001",
                        request
                );

        assertEquals(
                new BigDecimal("7500.00"),
                account.getBalance()
        );

        assertNotNull(result);

        verify(accountRepository)
                .save(account);

        verify(transactionRepository)
                .save(any(Transaction.class));
    }

    @Test
    void withdraw_shouldThrowException_whenBalanceIsInsufficient() {

        when(currentUserService.getCurrentCustomerId())
                .thenReturn(1L);

        when(accountRepository
                .findByAccountNumberAndCustomerIdForUpdate(
                        "FIN10000001",
                        1L
                ))
                .thenReturn(Optional.of(account));

        WithdrawalRequestDto request =
                new WithdrawalRequestDto();

        request.setAmount(
                new BigDecimal("15000.00")
        );

        assertThrows(
                InsufficientBalanceException.class,
                () -> transactionService.withdraw(
                        "FIN10000001",
                        request
                )
        );

        assertEquals(
                new BigDecimal("10000.00"),
                account.getBalance()
        );

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }
    @Test
    void transfer_shouldMoveMoneyBetweenAccounts() {

        Long customerId = 1L;

        TransferRequestDto request =
                new TransferRequestDto();

        request.setDestinationAccountNumber(
                "FIN10000002"
        );

        request.setAmount(
                new BigDecimal("3000.00")
        );

        request.setDescription(
                "Test transfer"
        );

        when(currentUserService.getCurrentCustomerId())
                .thenReturn(customerId);

        when(accountRepository
                .findByAccountNumberAndCustomerIdForUpdate(
                        "FIN10000001",
                        customerId
                ))
                .thenReturn(Optional.of(account));

        when(accountRepository
                .findByAccountNumber("FIN10000002"))
                .thenReturn(Optional.of(destinationAccount));

        when(transactionRepository.existsByReference(anyString()))
                .thenReturn(false);

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponseDto result =
                transactionService.transfer(
                        "FIN10000001",
                        request
                );

        assertEquals(
                new BigDecimal("7000.00"),
                account.getBalance()
        );

        assertEquals(
                new BigDecimal("8000.00"),
                destinationAccount.getBalance()
        );

        assertNotNull(result);

        verify(accountRepository)
                .save(account);

        verify(accountRepository)
                .save(destinationAccount);

        verify(transactionRepository)
                .save(any(Transaction.class));
    }

    @Test
    void transfer_shouldRejectSameAccountTransfer() {

        TransferRequestDto request =
                new TransferRequestDto();

        request.setDestinationAccountNumber(
                "FIN10000001"
        );

        request.setAmount(
                new BigDecimal("1000.00")
        );

        assertThrows(
                SameAccountTransferException.class,
                () -> transactionService.transfer(
                        "FIN10000001",
                        request
                )
        );

        verifyNoInteractions(accountRepository);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void transfer_shouldReject_whenInsufficientBalance() {

        when(currentUserService.getCurrentCustomerId())
                .thenReturn(1L);

        when(accountRepository
                .findByAccountNumberAndCustomerIdForUpdate(
                        "FIN10000001",
                        1L
                ))
                .thenReturn(Optional.of(account));

        when(accountRepository
                .findByAccountNumber("FIN10000002"))
                .thenReturn(Optional.of(destinationAccount));

        TransferRequestDto request =
                new TransferRequestDto();

        request.setDestinationAccountNumber(
                "FIN10000002"
        );

        request.setAmount(
                new BigDecimal("15000.00")
        );

        assertThrows(
                InsufficientBalanceException.class,
                () -> transactionService.transfer(
                        "FIN10000001",
                        request
                )
        );

        assertEquals(
                new BigDecimal("10000.00"),
                account.getBalance()
        );

        assertEquals(
                new BigDecimal("5000.00"),
                destinationAccount.getBalance()
        );

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void deposit_shouldRejectAccountBelongingToAnotherCustomer() {

        when(currentUserService.getCurrentCustomerId())
                .thenReturn(999L);

        when(accountRepository
                .findByAccountNumberAndCustomerIdForUpdate(
                        "FIN10000001",
                        999L
                ))
                .thenReturn(Optional.empty());

        DepositRequestDto request =
                new DepositRequestDto();

        request.setAmount(
                new BigDecimal("500.00")
        );

        assertThrows(
                AccountNotFoundException.class,
                () -> transactionService.deposit(
                        "FIN10000001",
                        request
                )
        );

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void getTransactionByReference_shouldReturnTransactionForOwner() {

        Transaction transaction = new Transaction();

        transaction.setReference("TXN-123456789");
        transaction.setAmount(
                new BigDecimal("1000.00")
        );
        transaction.setType(
                TransactionType.DEPOSIT
        );
        transaction.setStatus(
                TransactionStatus.SUCCESS
        );
        transaction.setDestinationAccount(account);

        when(currentUserService.getCurrentCustomerId())
                .thenReturn(1L);

        when(transactionRepository
                .findByReference("TXN-123456789"))
                .thenReturn(Optional.of(transaction));

        TransactionResponseDto result =
                transactionService.getTransactionByReference(
                        "TXN-123456789"
                );

        assertNotNull(result);
    }


    @Test
    void getTransactionByReference_shouldRejectTransactionOfAnotherCustomer() {

        Customer otherCustomer = new Customer();
        otherCustomer.setId(999L);

        Account otherAccount = new Account();
        otherAccount.setId(99L);
        otherAccount.setAccountNumber("FIN99999999");
        otherAccount.setCustomer(otherCustomer);

        Transaction transaction = new Transaction();

        transaction.setReference("TXN-999999999");
        transaction.setDestinationAccount(otherAccount);

        when(currentUserService.getCurrentCustomerId())
                .thenReturn(1L);

        when(transactionRepository
                .findByReference("TXN-999999999"))
                .thenReturn(Optional.of(transaction));

        assertThrows(
                TransactionNotFoundException.class,
                () -> transactionService.getTransactionByReference(
                        "TXN-999999999"
                )
        );
    }


    @Test
    void getTransactionByReference_shouldThrowWhenReferenceDoesNotExist() {

        when(currentUserService.getCurrentCustomerId())
                .thenReturn(1L);

        when(transactionRepository.findByReference("TXN-UNKNOWN"))
                .thenReturn(Optional.empty());

        assertThrows(
                TransactionNotFoundException.class,
                () -> transactionService.getTransactionByReference(
                        "TXN-UNKNOWN"
                )
        );
    }


    @Test
    void withdraw_shouldThrowException_whenAccountIsInactive() {

        account.setStatus(AccountStatus.BLOCKED);

        when(currentUserService.getCurrentCustomerId())
                .thenReturn(1L);

        when(accountRepository
                .findByAccountNumberAndCustomerIdForUpdate(
                        "FIN10000001",
                        1L
                ))
                .thenReturn(Optional.of(account));

        WithdrawalRequestDto request =
                new WithdrawalRequestDto();

        request.setAmount(new BigDecimal("500.00"));

        assertThrows(
                AccountNotActiveException.class,
                () -> transactionService.withdraw(
                        "FIN10000001",
                        request
                )
        );

        assertEquals(
                new BigDecimal("10000.00"),
                account.getBalance()
        );

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void transfer_shouldThrowException_whenDestinationAccountDoesNotExist() {

        when(currentUserService.getCurrentCustomerId())
                .thenReturn(1L);

        when(accountRepository
                .findByAccountNumberAndCustomerIdForUpdate(
                        "FIN10000001",
                        1L
                ))
                .thenReturn(Optional.of(account));

        when(accountRepository
                .findByAccountNumber("FIN99999999"))
                .thenReturn(Optional.empty());

        TransferRequestDto request =
                new TransferRequestDto();

        request.setDestinationAccountNumber("FIN99999999");
        request.setAmount(new BigDecimal("1000.00"));

        assertThrows(
                AccountNotFoundException.class,
                () -> transactionService.transfer(
                        "FIN10000001",
                        request
                )
        );

        assertEquals(
                new BigDecimal("10000.00"),
                account.getBalance()
        );

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void transfer_shouldThrowException_whenDestinationAccountIsInactive() {

        destinationAccount.setStatus(AccountStatus.BLOCKED);

        when(currentUserService.getCurrentCustomerId())
                .thenReturn(1L);

        when(accountRepository
                .findByAccountNumberAndCustomerIdForUpdate(
                        "FIN10000001",
                        1L
                ))
                .thenReturn(Optional.of(account));

        when(accountRepository
                .findByAccountNumber("FIN10000002"))
                .thenReturn(Optional.of(destinationAccount));

        TransferRequestDto request =
                new TransferRequestDto();

        request.setDestinationAccountNumber("FIN10000002");
        request.setAmount(new BigDecimal("1000.00"));

        assertThrows(
                AccountNotActiveException.class,
                () -> transactionService.transfer(
                        "FIN10000001",
                        request
                )
        );

        assertEquals(
                new BigDecimal("10000.00"),
                account.getBalance()
        );

        assertEquals(
                new BigDecimal("5000.00"),
                destinationAccount.getBalance()
        );

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }


    @Test
    void deposit_shouldRejectNonPositiveAmount() {

        DepositRequestDto request = new DepositRequestDto();
        request.setAmount(BigDecimal.ZERO);

        assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.deposit(
                        "FIN10000001",
                        request
                )
        );

        verifyNoInteractions(
                currentUserService,
                accountRepository,
                transactionRepository
        );
    }

    @Test
    void deposit_shouldRejectNegativeAmount() {

        DepositRequestDto request = new DepositRequestDto();
        request.setAmount(new BigDecimal("-100.00"));

        assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.deposit(
                        "FIN10000001",
                        request
                )
        );

        verifyNoInteractions(
                currentUserService,
                accountRepository,
                transactionRepository
        );
    }

}