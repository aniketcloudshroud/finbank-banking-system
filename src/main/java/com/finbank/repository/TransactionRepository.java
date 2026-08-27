package com.finbank.repository;

import com.finbank.entity.*;
import org.springframework.boot.autoconfigure.data.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    boolean existsByReference(String reference);

    Page<Transaction> findBySourceAccountAccountNumberOrDestinationAccountAccountNumber(
            String sourceAccountNumber,
            String destinationAccountNumber,
            Pageable pageable
    );

    Optional<Transaction> findByReference(String reference);
}
