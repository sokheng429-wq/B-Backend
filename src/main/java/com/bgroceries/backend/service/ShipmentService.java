package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.ShipmentDto;
import java.time.LocalDateTime;
import java.util.List;

public interface ShipmentService {
    List<ShipmentDto> getAllShipments(String search, String searchBy, String status, String outlet, LocalDateTime startDate, LocalDateTime endDate);
    ShipmentDto getShipmentById(Long id);
    ShipmentDto createShipment(ShipmentDto dto);
    ShipmentDto updateShipment(Long id, ShipmentDto dto);
    void deleteShipment(Long id);
    String generateNextCode();
}
