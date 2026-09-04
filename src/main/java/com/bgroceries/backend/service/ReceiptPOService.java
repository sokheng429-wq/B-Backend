package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.ReceiptPODto;

import java.time.LocalDate;
import java.util.List;

public interface ReceiptPOService {

    List<ReceiptPODto> getAllReceiptPOs(
            String search,
            String searchBy,
            LocalDate fromDate,
            LocalDate toDate,
            String outlet,
            String status
    );

    ReceiptPODto getReceiptPOById(Long id);

    ReceiptPODto createReceiptPO(ReceiptPODto dto);

    ReceiptPODto updateReceiptPO(Long id, ReceiptPODto dto);

    ReceiptPODto updateStatus(Long id, String status);

    void deleteReceiptPO(Long id);

    String generateNextCode();
}
