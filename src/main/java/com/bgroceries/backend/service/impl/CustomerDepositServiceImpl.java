package com.bgroceries.backend.service.impl;

import com.bgroceries.backend.dto.CustomerDepositDto;
import com.bgroceries.backend.entity.Sale.CustomerDeposit;
import com.bgroceries.backend.repository.CustomerDepositRepository;
import com.bgroceries.backend.service.CustomerDepositService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerDepositServiceImpl implements CustomerDepositService {

    private final CustomerDepositRepository depositRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDepositDto> getAllDeposits(
            String search,
            String searchBy,
            String status,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        List<CustomerDeposit> list;

        boolean hasFilter = (search != null && !search.isBlank()) ||
                            (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) ||
                            startDate != null || endDate != null;

        if (!hasFilter) {
            list = depositRepository.findTop50ByOrderByCreatedAtDesc();
        } else if (search != null && !search.isBlank() && searchBy != null && !searchBy.equalsIgnoreCase("any")) {
            list = depositRepository.searchByField(searchBy.toLowerCase(), search.trim());
        } else {
            list = depositRepository.searchDeposits(
                    search != null ? search.trim() : null,
                    (status != null && !status.equalsIgnoreCase("ALL")) ? status.trim() : null,
                    startDate,
                    endDate
            );
        }

        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDepositDto getDepositById(Long id) {
        CustomerDeposit entity = depositRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer Deposit not found with id: " + id));
        return toDto(entity);
    }

    @Override
    public CustomerDepositDto createDeposit(CustomerDepositDto dto) {
        String code = (dto.getCode() != null && !dto.getCode().isBlank() && !dto.getCode().equalsIgnoreCase("Auto Generate Code"))
                ? dto.getCode().trim()
                : generateNextCode();

        CustomerDeposit entity = CustomerDeposit.builder()
                .code(code)
                .depositDate(dto.getDepositDate() != null ? dto.getDepositDate() : LocalDateTime.now())
                .amount(dto.getAmount() != null ? dto.getAmount() : BigDecimal.ZERO)
                .customerId(dto.getCustomerId())
                .customerName(dto.getCustomerName())
                .contact(dto.getContact())
                .paymentType(dto.getPaymentType() != null ? dto.getPaymentType() : "Cash")
                .reference(dto.getReference())
                .username(dto.getUsername() != null ? dto.getUsername() : "Admin")
                .status(dto.getStatus() != null ? dto.getStatus() : "NONE_VOID")
                .note(dto.getNote())
                .build();

        CustomerDeposit saved = depositRepository.save(entity);
        return toDto(saved);
    }

    @Override
    public CustomerDepositDto updateDeposit(Long id, CustomerDepositDto dto) {
        CustomerDeposit entity = depositRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer Deposit not found with id: " + id));

        if (dto.getDepositDate() != null) entity.setDepositDate(dto.getDepositDate());
        if (dto.getAmount() != null) entity.setAmount(dto.getAmount());
        if (dto.getCustomerId() != null) entity.setCustomerId(dto.getCustomerId());
        if (dto.getCustomerName() != null) entity.setCustomerName(dto.getCustomerName());
        if (dto.getContact() != null) entity.setContact(dto.getContact());
        if (dto.getPaymentType() != null) entity.setPaymentType(dto.getPaymentType());
        if (dto.getReference() != null) entity.setReference(dto.getReference());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        if (dto.getNote() != null) entity.setNote(dto.getNote());

        CustomerDeposit saved = depositRepository.save(entity);
        return toDto(saved);
    }

    @Override
    public CustomerDepositDto updateStatus(Long id, String status) {
        CustomerDeposit entity = depositRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer Deposit not found with id: " + id));
        entity.setStatus(status != null ? status.toUpperCase() : "NONE_VOID");
        return toDto(depositRepository.save(entity));
    }

    @Override
    public void deleteDeposit(Long id) {
        depositRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public String generateNextCode() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "DEP-" + today + "-";
        long count = depositRepository.count() + 1;
        return String.format("%s%04d", prefix, count);
    }

    private CustomerDepositDto toDto(CustomerDeposit entity) {
        return CustomerDepositDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .depositDate(entity.getDepositDate())
                .amount(entity.getAmount())
                .customerId(entity.getCustomerId())
                .customerName(entity.getCustomerName())
                .contact(entity.getContact())
                .paymentType(entity.getPaymentType())
                .reference(entity.getReference())
                .username(entity.getUsername())
                .status(entity.getStatus())
                .note(entity.getNote())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
