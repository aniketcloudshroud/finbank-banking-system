package com.finbank.repository;

import com.finbank.entity.Customer;
import com.finbank.entity.KycStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository
        extends JpaRepository<Customer, Long> {

    boolean existsByEmail(String email);

    Page<Customer> findByKycStatus(
            KycStatus kycStatus,
            Pageable pageable
    );
}