package com.bgroceries.backend.service.impl;

import com.bgroceries.backend.dto.ArCollectionDto;
import com.bgroceries.backend.dto.ArCollectionInvoiceDto;
import com.bgroceries.backend.entity.Sale.ArCollection;
import com.bgroceries.backend.entity.Sale.ArCollectionInvoice;
import com.bgroceries.backend.repository.ArCollectionRepository;
import com.bgroceries.backend.service.ArCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ArCollectionServiceImpl implements ArCollectionService {

    private final ArCollectionRepository collectionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ArCollectionDto> getAllCollections(
            String search,
            String searchBy,
            String status,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        List<ArCollection> list;

        boolean hasFilter = (search != null && !search.isBlank()) ||
                            (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) ||
                            startDate != null || endDate != null;

        if (!hasFilter) {
            list = collectionRepository.findTop50ByOrderByCreatedAtDesc();
        } else if (search != null && !search.isBlank() && searchBy != null && !searchBy.equalsIgnoreCase("any")) {
            list = collectionRepository.searchByField(searchBy.toLowerCase(), search.trim());
        } else {
            list = collectionRepository.searchCollections(
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
    public ArCollectionDto getCollectionById(Long id) {
        ArCollection entity = collectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AR Collection not found with id: " + id));
        return toDto(entity);
    }

    @Override
    public ArCollectionDto createCollection(ArCollectionDto dto) {
        String code = (dto.getCode() != null && !dto.getCode().isBlank() && !dto.getCode().equalsIgnoreCase("Auto Generate Code"))
                ? dto.getCode().trim()
                : generateNextCode();

        BigDecimal paidTotal = BigDecimal.ZERO;
        if (dto.getInvoices() != null) {
            for (ArCollectionInvoiceDto inv : dto.getInvoices()) {
                if (inv.getPayAmount() != null) {
                    paidTotal = paidTotal.add(inv.getPayAmount());
                }
            }
        }
        if (paidTotal.compareTo(BigDecimal.ZERO) == 0 && dto.getPaidAmount() != null) {
            paidTotal = dto.getPaidAmount();
        }

        BigDecimal currentAmount = dto.getCurrentAmount() != null ? dto.getCurrentAmount() : paidTotal;
        BigDecimal remainAmount = currentAmount.subtract(paidTotal);
        if (remainAmount.compareTo(BigDecimal.ZERO) < 0) {
            remainAmount = BigDecimal.ZERO;
        }

        ArCollection entity = ArCollection.builder()
                .code(code)
                .paymentDate(dto.getPaymentDate() != null ? dto.getPaymentDate() : LocalDateTime.now())
                .rate(dto.getRate() != null ? dto.getRate() : new BigDecimal("4100.00"))
                .paidAmount(paidTotal)
                .balance(dto.getBalance() != null ? dto.getBalance() : BigDecimal.ZERO)
                .currentAmount(currentAmount)
                .remainAmount(remainAmount)
                .customerId(dto.getCustomerId())
                .customer(dto.getCustomer() != null ? dto.getCustomer() : "Valued Customer")
                .contact(dto.getContact())
                .user(dto.getUser() != null ? dto.getUser() : "Admin")
                .status(dto.getStatus() != null ? dto.getStatus() : "NONE_VOID")
                .note(dto.getNote())
                .applyMethod(dto.getApplyMethod() != null ? dto.getApplyMethod() : "FIFO")
                .paymentType(dto.getPaymentType() != null ? dto.getPaymentType() : "Cash")
                .authorizationNote(dto.getAuthorizationNote())
                .invoices(new ArrayList<>())
                .build();

        if (dto.getInvoices() != null) {
            for (ArCollectionInvoiceDto invDto : dto.getInvoices()) {
                ArCollectionInvoice inv = ArCollectionInvoice.builder()
                        .arCollection(entity)
                        .invCode(invDto.getInvCode())
                        .invDate(invDto.getInvDate())
                        .dueDate(invDto.getDueDate())
                        .currency(invDto.getCurrency() != null ? invDto.getCurrency() : "USD")
                        .rate(invDto.getRate() != null ? invDto.getRate() : entity.getRate())
                        .amount(invDto.getAmount() != null ? invDto.getAmount() : BigDecimal.ZERO)
                        .balance(invDto.getBalance() != null ? invDto.getBalance() : BigDecimal.ZERO)
                        .discount(invDto.getDiscount() != null ? invDto.getDiscount() : BigDecimal.ZERO)
                        .payAmount(invDto.getPayAmount() != null ? invDto.getPayAmount() : BigDecimal.ZERO)
                        .payCurrency(invDto.getPayCurrency() != null ? invDto.getPayCurrency() : "USD")
                        .build();
                entity.getInvoices().add(inv);
            }
        }

        ArCollection saved = collectionRepository.save(entity);
        return toDto(saved);
    }

    @Override
    public ArCollectionDto updateCollection(Long id, ArCollectionDto dto) {
        ArCollection entity = collectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AR Collection not found with id: " + id));

        if (dto.getPaymentDate() != null) entity.setPaymentDate(dto.getPaymentDate());
        if (dto.getRate() != null) entity.setRate(dto.getRate());
        if (dto.getCustomer() != null) entity.setCustomer(dto.getCustomer());
        if (dto.getContact() != null) entity.setContact(dto.getContact());
        if (dto.getNote() != null) entity.setNote(dto.getNote());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        if (dto.getApplyMethod() != null) entity.setApplyMethod(dto.getApplyMethod());
        if (dto.getPaymentType() != null) entity.setPaymentType(dto.getPaymentType());
        if (dto.getAuthorizationNote() != null) entity.setAuthorizationNote(dto.getAuthorizationNote());

        ArCollection saved = collectionRepository.save(entity);
        return toDto(saved);
    }

    @Override
    public ArCollectionDto updateStatus(Long id, String status) {
        ArCollection entity = collectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AR Collection not found with id: " + id));
        entity.setStatus(status != null ? status.toUpperCase() : "NONE_VOID");
        return toDto(collectionRepository.save(entity));
    }

    @Override
    public void deleteCollection(Long id) {
        collectionRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public String generateNextCode() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "ARC-" + today + "-";
        long count = collectionRepository.count() + 1;
        return String.format("%s%04d", prefix, count);
    }

    private ArCollectionDto toDto(ArCollection entity) {
        List<ArCollectionInvoiceDto> invoiceDtos = entity.getInvoices() != null
                ? entity.getInvoices().stream().map(this::toInvoiceDto).collect(Collectors.toList())
                : new ArrayList<>();

        return ArCollectionDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .paymentDate(entity.getPaymentDate())
                .rate(entity.getRate())
                .paidAmount(entity.getPaidAmount())
                .balance(entity.getBalance())
                .currentAmount(entity.getCurrentAmount())
                .remainAmount(entity.getRemainAmount())
                .customerId(entity.getCustomerId())
                .customer(entity.getCustomer())
                .contact(entity.getContact())
                .user(entity.getUser())
                .status(entity.getStatus())
                .note(entity.getNote())
                .applyMethod(entity.getApplyMethod())
                .paymentType(entity.getPaymentType())
                .authorizationNote(entity.getAuthorizationNote())
                .invoices(invoiceDtos)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private ArCollectionInvoiceDto toInvoiceDto(ArCollectionInvoice entity) {
        return ArCollectionInvoiceDto.builder()
                .id(entity.getId())
                .invCode(entity.getInvCode())
                .invDate(entity.getInvDate())
                .dueDate(entity.getDueDate())
                .currency(entity.getCurrency())
                .rate(entity.getRate())
                .amount(entity.getAmount())
                .balance(entity.getBalance())
                .discount(entity.getDiscount())
                .payAmount(entity.getPayAmount())
                .payCurrency(entity.getPayCurrency())
                .build();
    }
}
