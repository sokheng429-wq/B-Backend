package com.bgroceries.backend.service.impl;

import com.bgroceries.backend.dto.ReturnShipmentDto;
import com.bgroceries.backend.entity.Sale.ReturnShipment;
import com.bgroceries.backend.repository.ReturnShipmentRepository;
import com.bgroceries.backend.service.ReturnShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReturnShipmentServiceImpl implements ReturnShipmentService {
    private final ReturnShipmentRepository returnShipmentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ReturnShipmentDto> getAllReturnShipments(String search, String searchBy, String status, String outlet, LocalDateTime startDate, LocalDateTime endDate) {
        List<ReturnShipment> list;
        if ((search != null && !search.isBlank()) || (status != null && !status.equalsIgnoreCase("ALL")) || (outlet != null && !outlet.equalsIgnoreCase("ALL")) || startDate != null || endDate != null) {
            list = returnShipmentRepository.searchReturnShipments(search, status, outlet, startDate, endDate);
        } else {
            list = returnShipmentRepository.findTop50ByOrderByCreatedAtDesc();
        }
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnShipmentDto getReturnShipmentById(Long id) {
        return toDto(returnShipmentRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Return Shipment not found: " + id)));
    }

    @Override
    @Transactional
    public ReturnShipmentDto createReturnShipment(ReturnShipmentDto dto) {
        if (dto.getReturnShipCode() == null || dto.getReturnShipCode().isBlank() || dto.getReturnShipCode().equalsIgnoreCase("AUTO")) {
            dto.setReturnShipCode(generateNextCode());
        }
        ReturnShipment entity = toEntity(dto);
        return toDto(returnShipmentRepository.save(entity));
    }

    @Override
    @Transactional
    public ReturnShipmentDto updateReturnShipment(Long id, ReturnShipmentDto dto) {
        ReturnShipment existing = returnShipmentRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Return Shipment not found: " + id));
        existing.setSoCode(dto.getSoCode());
        existing.setCustomer(dto.getCustomer());
        existing.setDeliveryPerson(dto.getDeliveryPerson());
        existing.setAmount(dto.getAmount() != null ? dto.getAmount() : BigDecimal.ZERO);
        existing.setStatus(dto.getStatus() != null ? dto.getStatus() : existing.getStatus());
        existing.setOutlet(dto.getOutlet());
        existing.setUsername(dto.getUsername());
        return toDto(returnShipmentRepository.save(existing));
    }

    @Override
    @Transactional
    public void deleteReturnShipment(Long id) {
        returnShipmentRepository.deleteById(id);
    }

    @Override
    public String generateNextCode() {
        return "RET-" + DateTimeFormatter.ofPattern("yyyyMM").format(LocalDateTime.now()) + "-" + String.format("%04d", returnShipmentRepository.count() + 1);
    }

    private ReturnShipmentDto toDto(ReturnShipment entity) {
        return ReturnShipmentDto.builder()
                .id(entity.getId())
                .returnShipCode(entity.getReturnShipCode())
                .soCode(entity.getSoCode())
                .date(entity.getDate())
                .customer(entity.getCustomer())
                .deliveryPerson(entity.getDeliveryPerson())
                .amount(entity.getAmount())
                .status(entity.getStatus())
                .outlet(entity.getOutlet())
                .username(entity.getUsername())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private ReturnShipment toEntity(ReturnShipmentDto dto) {
        return ReturnShipment.builder()
                .returnShipCode(dto.getReturnShipCode())
                .soCode(dto.getSoCode())
                .date(dto.getDate() != null ? dto.getDate() : LocalDateTime.now())
                .customer(dto.getCustomer())
                .deliveryPerson(dto.getDeliveryPerson())
                .amount(dto.getAmount() != null ? dto.getAmount() : BigDecimal.ZERO)
                .status(dto.getStatus() != null ? dto.getStatus() : "RECEIVED")
                .outlet(dto.getOutlet() != null ? dto.getOutlet() : "Main Store")
                .username(dto.getUsername() != null ? dto.getUsername() : "inspector")
                .build();
    }
}
