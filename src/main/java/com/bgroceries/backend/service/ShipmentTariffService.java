package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.ShipmentTariffDto;

import java.util.List;

public interface ShipmentTariffService {
    List<ShipmentTariffDto> getAll(String search, String searchBy, String status);
    ShipmentTariffDto getById(Long id);
    ShipmentTariffDto create(ShipmentTariffDto dto);
    ShipmentTariffDto update(Long id, ShipmentTariffDto dto);
    ShipmentTariffDto updateStatus(Long id, Boolean active);
    void delete(Long id);
    String generateNextCode();
}
