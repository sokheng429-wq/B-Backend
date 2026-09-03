package com.bgroceries.backend.service.impl;

import com.bgroceries.backend.dto.ShipmentDto;
import com.bgroceries.backend.entity.Sale.Shipment;
import com.bgroceries.backend.repository.ShipmentRepository;
import com.bgroceries.backend.service.ShipmentService;
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
public class ShipmentServiceImpl implements ShipmentService {
    private final ShipmentRepository shipmentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentDto> getAllShipments(String search, String searchBy, String status, String outlet, LocalDateTime startDate, LocalDateTime endDate) {
        List<Shipment> list;
        if ((search != null && !search.isBlank()) || (status != null && !status.equalsIgnoreCase("ALL")) || (outlet != null && !outlet.equalsIgnoreCase("ALL")) || startDate != null || endDate != null) {
            list = shipmentRepository.searchShipments(search, status, outlet, startDate, endDate);
        } else {
            list = shipmentRepository.findTop50ByOrderByCreatedAtDesc();
        }
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentDto getShipmentById(Long id) {
        return toDto(shipmentRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Shipment not found: " + id)));
    }

    @Override
    @Transactional
    public ShipmentDto createShipment(ShipmentDto dto) {
        if (dto.getShipCode() == null || dto.getShipCode().isBlank() || dto.getShipCode().equalsIgnoreCase("AUTO")) {
            dto.setShipCode(generateNextCode());
        }
        Shipment entity = toEntity(dto);
        return toDto(shipmentRepository.save(entity));
    }

    @Override
    @Transactional
    public ShipmentDto updateShipment(Long id, ShipmentDto dto) {
        Shipment existing = shipmentRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Shipment not found: " + id));
        existing.setCustomer(dto.getCustomer());
        existing.setPhone(dto.getPhone());
        existing.setAmount(dto.getAmount() != null ? dto.getAmount() : BigDecimal.ZERO);
        existing.setBalance(dto.getBalance() != null ? dto.getBalance() : BigDecimal.ZERO);
        existing.setDeliveryPerson(dto.getDeliveryPerson());
        existing.setStatus(dto.getStatus() != null ? dto.getStatus() : existing.getStatus());
        existing.setSalesperson(dto.getSalesperson());
        existing.setReference(dto.getReference());
        existing.setUsername(dto.getUsername());
        existing.setOutlet(dto.getOutlet());
        existing.setCarrier(dto.getCarrier());
        existing.setDestination(dto.getDestination());
        return toDto(shipmentRepository.save(existing));
    }

    @Override
    @Transactional
    public void deleteShipment(Long id) {
        shipmentRepository.deleteById(id);
    }

    @Override
    public String generateNextCode() {
        return "SHP-" + DateTimeFormatter.ofPattern("yyyyMM").format(LocalDateTime.now()) + "-" + String.format("%04d", shipmentRepository.count() + 1);
    }

    private ShipmentDto toDto(Shipment entity) {
        return ShipmentDto.builder()
                .id(entity.getId())
                .shipCode(entity.getShipCode())
                .date(entity.getDate())
                .customer(entity.getCustomer())
                .phone(entity.getPhone())
                .balance(entity.getBalance())
                .amount(entity.getAmount())
                .deliveryPerson(entity.getDeliveryPerson())
                .status(entity.getStatus())
                .salesperson(entity.getSalesperson())
                .reference(entity.getReference())
                .username(entity.getUsername())
                .outlet(entity.getOutlet())
                .carrier(entity.getCarrier())
                .destination(entity.getDestination())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private Shipment toEntity(ShipmentDto dto) {
        return Shipment.builder()
                .shipCode(dto.getShipCode())
                .date(dto.getDate() != null ? dto.getDate() : LocalDateTime.now())
                .customer(dto.getCustomer())
                .phone(dto.getPhone())
                .balance(dto.getBalance() != null ? dto.getBalance() : BigDecimal.ZERO)
                .amount(dto.getAmount() != null ? dto.getAmount() : BigDecimal.ZERO)
                .deliveryPerson(dto.getDeliveryPerson())
                .status(dto.getStatus() != null ? dto.getStatus() : "READY")
                .salesperson(dto.getSalesperson())
                .reference(dto.getReference())
                .username(dto.getUsername() != null ? dto.getUsername() : "dispatcher")
                .outlet(dto.getOutlet() != null ? dto.getOutlet() : "Main Store")
                .carrier(dto.getCarrier() != null ? dto.getCarrier() : "Internal Fleet Driver")
                .destination(dto.getDestination())
                .build();
    }
}
