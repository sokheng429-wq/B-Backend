package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.RequisitionDto;

import java.time.LocalDate;
import java.util.List;

public interface RequisitionService {

    List<RequisitionDto> getAllRequisitions(
            String search,
            String searchBy,
            LocalDate fromDate,
            LocalDate toDate,
            String status
    );

    RequisitionDto getRequisitionById(Long id);

    RequisitionDto createRequisition(RequisitionDto dto);

    RequisitionDto updateRequisition(Long id, RequisitionDto dto);

    RequisitionDto updateStatus(Long id, String status);

    void deleteRequisition(Long id);

    String generateNextCode();
}
