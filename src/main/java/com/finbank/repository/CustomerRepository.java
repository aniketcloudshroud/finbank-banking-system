package com.finbank.repository;

import com.finbank.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;


public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Page<Customer> findByKycStatus(
            KycStatus kycStatus,
            Pageable pageable
    );

}
