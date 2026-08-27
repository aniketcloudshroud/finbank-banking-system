package com.finbank.service;

import com.finbank.dto.KycRequestDto;
import com.finbank.dto.KycResponseDto;
import com.finbank.entity.Customer;
import com.finbank.entity.KycStatus;
import com.finbank.entity.User;
import com.finbank.exception.CustomerNotFoundException;
import com.finbank.repository.CustomerRepository;
import com.finbank.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

@Service
public class KycService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    public KycService(
            CustomerRepository customerRepository,
            UserRepository userRepository
    ) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public KycResponseDto submitKyc(
            Long customerId,
            KycRequestDto request
    ) {

        Customer customer = getCustomer(customerId);

        verifyOwnership(customer);

        if (customer.getKycStatus() == KycStatus.APPROVED) {
            throw new IllegalStateException(
                    "KYC has already been approved"
            );
        }

        if (customer.getKycStatus() == KycStatus.UNDER_REVIEW) {
            throw new IllegalStateException(
                    "KYC is already under review"
            );
        }

        customer.setKycDocumentType(request.getDocumentType());
        customer.setKycDocumentNumber(request.getDocumentNumber());
        customer.setKycStatus(KycStatus.UNDER_REVIEW);
        customer.setKycSubmittedAt(LocalDateTime.now());
        customer.setKycReviewedAt(null);
        customer.setKycRejectionReason(null);

        Customer savedCustomer =
                customerRepository.save(customer);

        return new KycResponseDto(savedCustomer);
    }


    public KycResponseDto getKyc(Long customerId) {

        Customer customer = getCustomer(customerId);

        verifyOwnership(customer);

        return new KycResponseDto(customer);
    }


    @Transactional
    public KycResponseDto approveKyc(Long customerId) {

        Customer customer = getCustomer(customerId);

        if (customer.getKycStatus() != KycStatus.UNDER_REVIEW) {
            throw new IllegalStateException(
                    "Only KYC applications under review can be approved"
            );
        }

        customer.setKycStatus(KycStatus.APPROVED);
        customer.setKycReviewedAt(LocalDateTime.now());
        customer.setKycRejectionReason(null);

        Customer savedCustomer =
                customerRepository.save(customer);

        return new KycResponseDto(savedCustomer);
    }


    @Transactional
    public KycResponseDto rejectKyc(
            Long customerId,
            String reason
    ) {

        Customer customer = getCustomer(customerId);

        if (customer.getKycStatus() != KycStatus.UNDER_REVIEW) {
            throw new IllegalStateException(
                    "Only KYC applications under review can be rejected"
            );
        }

        customer.setKycStatus(KycStatus.REJECTED);
        customer.setKycReviewedAt(LocalDateTime.now());
        customer.setKycRejectionReason(reason);

        Customer savedCustomer =
                customerRepository.save(customer);

        return new KycResponseDto(savedCustomer);
    }


    private Customer getCustomer(Long customerId) {

        return customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new CustomerNotFoundException(
                                "Customer with id " +
                                        customerId +
                                        " not found"
                        )
                );
    }


    private void verifyOwnership(Customer customer) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User with email " +
                                        email +
                                        " not found"
                        )
                );

        Long authenticatedCustomerId =
                user.getCustomer().getId();

        if (!authenticatedCustomerId.equals(customer.getId())) {
            throw new SecurityException(
                    "You are not authorized to access this customer's KYC"
            );
        }
    }

    public Page<KycResponseDto> getPendingKyc(Pageable pageable) {

        return customerRepository
                .findByKycStatus(KycStatus.UNDER_REVIEW, pageable)
                .map(KycResponseDto::new);
    }
}