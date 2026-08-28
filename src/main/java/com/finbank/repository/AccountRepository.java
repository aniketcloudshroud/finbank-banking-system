package com.finbank.repository;

import com.finbank.entity.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    Optional<Account> findByAccountNumberAndCustomerId(
            String accountNumber,
            Long customerId
    );

    Page<Account> findByCustomerId(
            Long customerId,
            Pageable pageable
    );

    boolean existsByAccountNumber(
            String accountNumber
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT a
            FROM Account a
            WHERE a.accountNumber = :accountNumber
            AND a.customer.id = :customerId
            """)
    Optional<Account> findByAccountNumberAndCustomerIdForUpdate(
            @Param("accountNumber") String accountNumber,
            @Param("customerId") Long customerId
    );
}