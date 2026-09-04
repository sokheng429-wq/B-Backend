package com.bgroceries.backend.service.impl;

import com.bgroceries.backend.dto.ShipmentMethodDto;
import com.bgroceries.backend.dto.ShipmentTariffDto;
import com.bgroceries.backend.entity.Freight.ShipmentMethod;
import com.bgroceries.backend.entity.Freight.ShipmentTariff;
import com.bgroceries.backend.repository.ShipmentMethodRepository;
import com.bgroceries.backend.repository.ShipmentTariffRepository;
import com.bgroceries.backend.service.ShipmentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ShipmentMethodServiceImpl implements ShipmentMethodService {

    private final ShipmentMethodRepository shipmentMethodRepository;
    private final ShipmentTariffRepository shipmentTariffRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentMethodDto> getAll(String search, String searchBy, String status) {
        List<ShipmentMethod> list = shipmentMethodRepository.findAll();

        // Filter by Status: Active, All, Inactive
        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) {
            boolean requireActive = status.equalsIgnoreCase("ACTIVE") || status.equalsIgnoreCase("TRUE");
            list = list.stream()
                    .filter(t -> t.getActive() != null && t.getActive() == requireActive)
                    .collect(Collectors.toList());
        }

        // Filter by Search & SearchBy (Any, Code, Description, Second Language)
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
                    case "cost proration":
                    case "costproration":
                        return t.getCostProration() != null && t.getCostProration().toLowerCase().contains(q);
                    case "any":
                    default:
                        return (t.getCode() != null && t.getCode().toLowerCase().contains(q)) ||
                               (t.getDescription() != null && t.getDescription().toLowerCase().contains(q)) ||
                               (t.getSecondLanguage() != null && t.getSecondLanguage().toLowerCase().contains(q)) ||
                               (t.getCostProration() != null && t.getCostProration().toLowerCase().contains(q));
                }
            }).collect(Collectors.toList());
        }

        // Sort by ID descending (newest first)
        list.sort((a, b) -> Long.compare(b.getId() != null ? b.getId() : 0, a.getId() != null ? a.getId() : 0));

        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentMethodDto getById(Long id) {
        ShipmentMethod entity = shipmentMethodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipment Method not found with id: " + id));
        return toDto(entity);
    }

    @Override
    public ShipmentMethodDto create(ShipmentMethodDto dto) {
        String code = (dto.getCode() != null && !dto.getCode().isBlank() && !dto.getCode().equalsIgnoreCase("Auto Generate Code"))
                ? dto.getCode().trim()
                : generateNextCode();

        String costProration = (dto.getCostProration() != null && !dto.getCostProration().isBlank())
                ? dto.getCostProration().trim().toUpperCase()
                : "VALUE";

        ShipmentMethod entity = ShipmentMethod.builder()
                .code(code)
                .description(dto.getDescription() != null ? dto.getDescription().trim() : "")
                .secondLanguage(dto.getSecondLanguage() != null ? dto.getSecondLanguage().trim() : null)
                .costProration(costProration)
                .active(dto.getActive() != null ? dto.getActive() : true)
                .tariffs(new ArrayList<>())
                .build();

        if (dto.getTariffIds() != null && !dto.getTariffIds().isEmpty()) {
            List<ShipmentTariff> tariffs = shipmentTariffRepository.findAllById(dto.getTariffIds());
            entity.setTariffs(tariffs);
        }

        ShipmentMethod saved = shipmentMethodRepository.save(entity);
        return toDto(saved);
    }

    @Override
    public ShipmentMethodDto update(Long id, ShipmentMethodDto dto) {
        ShipmentMethod entity = shipmentMethodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipment Method not found with id: " + id));

        if (dto.getCode() != null && !dto.getCode().isBlank()) {
            entity.setCode(dto.getCode().trim());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription().trim());
        }
        if (dto.getSecondLanguage() != null) {
            entity.setSecondLanguage(dto.getSecondLanguage().trim());
        }
        if (dto.getCostProration() != null && !dto.getCostProration().isBlank()) {
            entity.setCostProration(dto.getCostProration().trim().toUpperCase());
        }
        if (dto.getActive() != null) {
            entity.setActive(dto.getActive());
        }

        if (dto.getTariffIds() != null) {
            List<ShipmentTariff> tariffs = shipmentTariffRepository.findAllById(dto.getTariffIds());
            entity.setTariffs(tariffs);
        }

        ShipmentMethod saved = shipmentMethodRepository.save(entity);
        return toDto(saved);
    }

    @Override
    public ShipmentMethodDto updateStatus(Long id, Boolean active) {
        ShipmentMethod entity = shipmentMethodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipment Method not found with id: " + id));
        entity.setActive(active != null ? active : !Boolean.TRUE.equals(entity.getActive()));
        return toDto(shipmentMethodRepository.save(entity));
    }

    @Override
    public void delete(Long id) {
        shipmentMethodRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public String generateNextCode() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = shipmentMethodRepository.count() + 1;
        String candidate = String.format("SM-%s-%04d", datePrefix, count);
        int suffix = 1;
        while (shipmentMethodRepository.existsByCode(candidate)) {
            candidate = String.format("SM-%s-%04d", datePrefix, count + suffix);
            suffix++;
        }
        return candidate;
    }

    private ShipmentMethodDto toDto(ShipmentMethod entity) {
        List<Long> tariffIds = new ArrayList<>();
        List<ShipmentTariffDto> tariffDtos = new ArrayList<>();

        if (entity.getTariffs() != null) {
            for (ShipmentTariff t : entity.getTariffs()) {
                tariffIds.add(t.getId());
                tariffDtos.add(ShipmentTariffDto.builder()
                        .id(t.getId())
                        .code(t.getCode())
                        .description(t.getDescription())
                        .secondLanguage(t.getSecondLanguage())
                        .supplierId(t.getSupplierId())
                        .supplier(t.getSupplier())
                        .active(t.getActive())
                        .createdAt(t.getCreatedAt())
                        .updatedAt(t.getUpdatedAt())
                        .build());
            }
        }

        return ShipmentMethodDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .description(entity.getDescription())
                .secondLanguage(entity.getSecondLanguage())
                .costProration(entity.getCostProration())
                .active(entity.getActive())
                .tariffIds(tariffIds)
                .tariffs(tariffDtos)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
