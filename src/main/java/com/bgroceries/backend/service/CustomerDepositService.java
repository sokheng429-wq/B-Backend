package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.CustomerDepositDto;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomerDepositService {

    List<CustomerDepositDto> getAllDeposits(
            String search,
            String searchBy,
            String status,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    CustomerDepositDto getDepositById(Long id);

    CustomerDepositDto createDeposit(CustomerDepositDto dto);

    CustomerDepositDto updateDeposit(Long id, CustomerDepositDto dto);

    CustomerDepositDto updateStatus(Long id, String status);

    void deleteDeposit(Long id);

    String generateNextCode();
}
