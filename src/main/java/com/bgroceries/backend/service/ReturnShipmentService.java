package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.ReturnShipmentDto;
import java.time.LocalDateTime;
import java.util.List;

public interface ReturnShipmentService {
    List<ReturnShipmentDto> getAllReturnShipments(String search, String searchBy, String status, String outlet, LocalDateTime startDate, LocalDateTime endDate);
    ReturnShipmentDto getReturnShipmentById(Long id);
    ReturnShipmentDto createReturnShipment(ReturnShipmentDto dto);
    ReturnShipmentDto updateReturnShipment(Long id, ReturnShipmentDto dto);
    void deleteReturnShipment(Long id);
    String generateNextCode();
}
