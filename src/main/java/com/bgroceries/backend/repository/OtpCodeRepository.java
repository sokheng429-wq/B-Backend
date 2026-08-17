package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.OtpCode;
import com.bgroceries.backend.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    Optional<OtpCode> findTopByPhoneNumberAndPurposeAndUsedFalseOrderByCreatedAtDesc(
            String phoneNumber, OtpPurpose purpose);
}
