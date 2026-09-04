package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.ReturnReceiptPODto;

import java.time.LocalDate;
import java.util.List;

public interface ReturnReceiptPOService {
    List<ReturnReceiptPODto> getAllReturnReceiptPOs(String search, String searchBy, LocalDate fromDate, LocalDate toDate, String outlet, String status);
    ReturnReceiptPODto getReturnReceiptPOById(Long id);
    ReturnReceiptPODto createReturnReceiptPO(ReturnReceiptPODto dto);
    ReturnReceiptPODto updateReturnReceiptPO(Long id, ReturnReceiptPODto dto);
    ReturnReceiptPODto updateStatus(Long id, String status);
    void deleteReturnReceiptPO(Long id);
    String generateNextCode();
}
