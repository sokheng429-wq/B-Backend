package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.QuotationDto;

import java.time.LocalDateTime;
import java.util.List;

public interface QuotationService {

    List<QuotationDto> getAllQuotations(
            String search,
            String searchBy,
            String status,
            String outlet,
            String customer,
            String salesperson,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    QuotationDto getQuotationById(Long id);

    QuotationDto getQuotationByCode(String code);

    QuotationDto createQuotation(QuotationDto dto);

    QuotationDto updateQuotation(Long id, QuotationDto dto);

    QuotationDto updateStatus(Long id, String status);

    void deleteQuotation(Long id);

    String generateNextCode();
}
