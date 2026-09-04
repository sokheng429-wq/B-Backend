package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.PaymentTermDto;

import java.util.List;

public interface PaymentTermService {

    List<PaymentTermDto> getAllPaymentTerms(String search, String searchBy, String status);

    PaymentTermDto getPaymentTermById(Long id);

    PaymentTermDto createPaymentTerm(PaymentTermDto dto);

    PaymentTermDto updatePaymentTerm(Long id, PaymentTermDto dto);

    PaymentTermDto updateStatus(Long id, Boolean active);

    void deletePaymentTerm(Long id);

    String generateNextCode();
}
