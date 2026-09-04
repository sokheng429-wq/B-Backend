package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.ShipmentMethodDto;

import java.util.List;

public interface ShipmentMethodService {
    List<ShipmentMethodDto> getAll(String search, String searchBy, String status);
    ShipmentMethodDto getById(Long id);
    ShipmentMethodDto create(ShipmentMethodDto dto);
    ShipmentMethodDto update(Long id, ShipmentMethodDto dto);
    ShipmentMethodDto updateStatus(Long id, Boolean active);
    void delete(Long id);
    String generateNextCode();
}
