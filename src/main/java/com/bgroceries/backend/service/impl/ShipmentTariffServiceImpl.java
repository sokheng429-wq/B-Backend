package com.bgroceries.backend.service.impl;

import com.bgroceries.backend.dto.ShipmentTariffDto;
import com.bgroceries.backend.entity.Freight.ShipmentTariff;
import com.bgroceries.backend.repository.ShipmentTariffRepository;
import com.bgroceries.backend.service.ShipmentTariffService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ShipmentTariffServiceImpl implements ShipmentTariffService {

    private final ShipmentTariffRepository shipmentTariffRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentTariffDto> getAll(String search, String searchBy, String status) {
        List<ShipmentTariff> list = shipmentTariffRepository.findAll();

        // Filter by Status: Active, All, Inactive
        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) {
            boolean requireActive = status.equalsIgnoreCase("ACTIVE") || status.equalsIgnoreCase("TRUE");
            list = list.stream()
                    .filter(t -> t.getActive() != null && t.getActive() == requireActive)
                    .collect(Collectors.toList());
        }

        // Filter by Search and SearchBy (Any, Code, Description, Second Language)
        if (search != null && !search.isBlank()) {
            String q = search.trim().toLowerCase();
            String field = (searchBy != null && !searchBy.isBlank()) ? searchBy.trim().toLowerCase() : "any";

            list = list.stream().filter(t -> {
                switch (field) {
                    case "code":
                        return t.getCode() != null && t.getCode().toLowerCase().contains(q);
                    case "description":
                        return t.getDescription() != null && t.getDescription().toLowerCase().contains(q);
                    case "second language":
                    case "secondlanguage":
                        return t.getSecondLanguage() != null && t.getSecondLanguage().toLowerCase().contains(q);
                    case "any":
                    default:
                        return (t.getCode() != null && t.getCode().toLowerCase().contains(q)) ||
                               (t.getDescription() != null && t.getDescription().toLowerCase().contains(q)) ||
                               (t.getSecondLanguage() != null && t.getSecondLanguage().toLowerCase().contains(q)) ||
                               (t.getSupplier() != null && t.getSupplier().toLowerCase().contains(q));
                }
            }).collect(Collectors.toList());
        }

        // Sort by ID descending (newest first)
        list.sort((a, b) -> Long.compare(b.getId() != null ? b.getId() : 0, a.getId() != null ? a.getId() : 0));

        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentTariffDto getById(Long id) {
        ShipmentTariff entity = shipmentTariffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipment Tariff not found with id: " + id));
        return toDto(entity);
    }

    @Override
    public ShipmentTariffDto create(ShipmentTariffDto dto) {
        String code = (dto.getCode() != null && !dto.getCode().isBlank() && !dto.getCode().equalsIgnoreCase("Auto Generate Code"))
                ? dto.getCode().trim()
                : generateNextCode();

        ShipmentTariff entity = ShipmentTariff.builder()
                .code(code)
                .description(dto.getDescription() != null ? dto.getDescription().trim() : "")
                .secondLanguage(dto.getSecondLanguage() != null ? dto.getSecondLanguage().trim() : null)
                .supplierId(dto.getSupplierId())
                .supplier(dto.getSupplier() != null ? dto.getSupplier().trim() : null)
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();

        ShipmentTariff saved = shipmentTariffRepository.save(entity);
        return toDto(saved);
    }

    @Override
    public ShipmentTariffDto update(Long id, ShipmentTariffDto dto) {
        ShipmentTariff entity = shipmentTariffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipment Tariff not found with id: " + id));

        if (dto.getCode() != null && !dto.getCode().isBlank()) {
            entity.setCode(dto.getCode().trim());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription().trim());
        }
        if (dto.getSecondLanguage() != null) {
            entity.setSecondLanguage(dto.getSecondLanguage().trim());
        }
        if (dto.getSupplierId() != null) {
            entity.setSupplierId(dto.getSupplierId());
        }
        if (dto.getSupplier() != null) {
            entity.setSupplier(dto.getSupplier().trim());
        }
        if (dto.getActive() != null) {
            entity.setActive(dto.getActive());
        }

        ShipmentTariff saved = shipmentTariffRepository.save(entity);
        return toDto(saved);
    }

    @Override
    public ShipmentTariffDto updateStatus(Long id, Boolean active) {
        ShipmentTariff entity = shipmentTariffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipment Tariff not found with id: " + id));
        entity.setActive(active != null ? active : !Boolean.TRUE.equals(entity.getActive()));
        return toDto(shipmentTariffRepository.save(entity));
    }

    @Override
    public void delete(Long id) {
        shipmentTariffRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public String generateNextCode() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = shipmentTariffRepository.count() + 1;
        String candidate = String.format("ST-%s-%04d", datePrefix, count);
        int suffix = 1;
        while (shipmentTariffRepository.existsByCode(candidate)) {
            candidate = String.format("ST-%s-%04d", datePrefix, count + suffix);
            suffix++;
        }
        return candidate;
    }

    private ShipmentTariffDto toDto(ShipmentTariff entity) {
        return ShipmentTariffDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .description(entity.getDescription())
                .secondLanguage(entity.getSecondLanguage())
                .supplierId(entity.getSupplierId())
                .supplier(entity.getSupplier())
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
