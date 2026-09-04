package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.CashOperationDto;

import java.time.LocalDateTime;
import java.util.List;

public interface CashOperationService {
    List<CashOperationDto> searchOperations(String search, String searchBy, String type, String outlet, String status, LocalDateTime fromDate, LocalDateTime toDate);
    CashOperationDto getById(Long id);
    CashOperationDto voidOperation(Long id);
    void seedInitialData();
}
