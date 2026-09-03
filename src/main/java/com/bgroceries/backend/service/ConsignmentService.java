package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.ConsignmentDto;

import java.time.LocalDateTime;
import java.util.List;

public interface ConsignmentService {

    List<ConsignmentDto> getAllConsignments(
            String search,
            String searchBy,
            String status,
            String outlet,
            String customer,
            String salesperson,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    ConsignmentDto getConsignmentById(Long id);

    ConsignmentDto getConsignmentByCode(String code);

    ConsignmentDto createConsignment(ConsignmentDto dto);

    ConsignmentDto updateConsignment(Long id, ConsignmentDto dto);

    ConsignmentDto updateStatus(Long id, String status);

    void deleteConsignment(Long id);

    String generateNextCode();
}
