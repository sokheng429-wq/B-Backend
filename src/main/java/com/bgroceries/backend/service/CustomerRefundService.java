package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.CustomerRefundDto;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomerRefundService {

    List<CustomerRefundDto> getAllRefunds(
            String search,
            String searchBy,
            String status,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    CustomerRefundDto getRefundById(Long id);

    CustomerRefundDto createRefund(CustomerRefundDto dto);

    CustomerRefundDto updateRefund(Long id, CustomerRefundDto dto);

    CustomerRefundDto updateStatus(Long id, String status);

    void deleteRefund(Long id);

    String generateNextCode();
}
