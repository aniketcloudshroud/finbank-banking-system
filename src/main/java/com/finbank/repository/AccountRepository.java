package com.finbank.repository;

import com.finbank.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository
        extends JpaRepository<Account, Long> {

    boolean existsByAccountNumber(
            String accountNumber
    );

    Optional<Account> findByAccountNumber(
            String accountNumber
    );

    Optional<Account> findByAccountNumberAndCustomerId(
            String accountNumber,
            Long customerId
    );

    Page<Account> findByCustomerId(
            Long customerId,
            Pageable pageable
    );
}