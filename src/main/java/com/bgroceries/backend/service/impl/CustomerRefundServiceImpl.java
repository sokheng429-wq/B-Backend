package com.bgroceries.backend.service.impl;

import com.bgroceries.backend.dto.CustomerRefundDto;
import com.bgroceries.backend.dto.CustomerRefundInvoiceDto;
import com.bgroceries.backend.entity.Sale.CustomerRefund;
import com.bgroceries.backend.entity.Sale.CustomerRefundInvoice;
import com.bgroceries.backend.repository.CustomerRefundRepository;
import com.bgroceries.backend.service.CustomerRefundService;
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
public class CustomerRefundServiceImpl implements CustomerRefundService {

    private final CustomerRefundRepository refundRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CustomerRefundDto> getAllRefunds(
            String search,
            String searchBy,
            String status,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        List<CustomerRefund> list = refundRepository.findAll();

        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) {
            String st = status.trim().toUpperCase();
            list = list.stream()
                    .filter(r -> r.getStatus() != null && r.getStatus().equalsIgnoreCase(st))
                    .collect(Collectors.toList());
        }

        if (startDate != null) {
            list = list.stream()
                    .filter(r -> r.getPaymentDate() != null && !r.getPaymentDate().isBefore(startDate))
                    .collect(Collectors.toList());
        }

        if (endDate != null) {
            list = list.stream()
                    .filter(r -> r.getPaymentDate() != null && !r.getPaymentDate().isAfter(endDate))
                    .collect(Collectors.toList());
        }

        if (search != null && !search.isBlank()) {
            String q = search.trim().toLowerCase();
            String field = (searchBy != null && !searchBy.isBlank()) ? searchBy.trim().toLowerCase() : "any";

            list = list.stream().filter(r -> {
                switch (field) {
                    case "code":
                        return r.getCode() != null && r.getCode().toLowerCase().contains(q);
                    case "amount":
                        return r.getPaidAmount() != null && r.getPaidAmount().toString().contains(q);
                    case "rate":
                        return r.getRate() != null && r.getRate().toString().contains(q);
                    case "partner":
                        return r.getPartner() != null && r.getPartner().toLowerCase().contains(q);
                    case "contact":
                        return (r.getContact() != null && r.getContact().toLowerCase().contains(q)) ||
                               (r.getPhone() != null && r.getPhone().toLowerCase().contains(q));
                    case "any":
                    default:
                        return (r.getCode() != null && r.getCode().toLowerCase().contains(q)) ||
                               (r.getPartner() != null && r.getPartner().toLowerCase().contains(q)) ||
                               (r.getContact() != null && r.getContact().toLowerCase().contains(q)) ||
                               (r.getPhone() != null && r.getPhone().toLowerCase().contains(q)) ||
                               (r.getUsername() != null && r.getUsername().toLowerCase().contains(q)) ||
                               (r.getPaidAmount() != null && r.getPaidAmount().toString().contains(q)) ||
                               (r.getRate() != null && r.getRate().toString().contains(q));
                }
            }).collect(Collectors.toList());
        }

        list.sort((a, b) -> Long.compare(b.getId() != null ? b.getId() : 0, a.getId() != null ? a.getId() : 0));

        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerRefundDto getRefundById(Long id) {
        CustomerRefund entity = refundRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer Refund not found with id: " + id));
        return toDto(entity);
    }

    @Override
    public CustomerRefundDto createRefund(CustomerRefundDto dto) {
        String code = (dto.getCode() != null && !dto.getCode().isBlank() && !dto.getCode().equalsIgnoreCase("Auto Generate Code"))
                ? dto.getCode().trim()
                : generateNextCode();

        BigDecimal paidTotal = BigDecimal.ZERO;
        if (dto.getInvoices() != null) {
            for (CustomerRefundInvoiceDto inv : dto.getInvoices()) {
                if (inv.getPayAmount() != null) {
                    paidTotal = paidTotal.add(inv.getPayAmount());
                }
            }
        }
        if (paidTotal.compareTo(BigDecimal.ZERO) == 0 && dto.getPaidAmount() != null) {
            paidTotal = dto.getPaidAmount();
        }

        CustomerRefund entity = CustomerRefund.builder()
                .code(code)
                .paymentDate(dto.getPaymentDate() != null ? dto.getPaymentDate() : LocalDateTime.now())
                .rate(dto.getRate() != null ? dto.getRate() : new BigDecimal("4100.00"))
                .paidAmount(paidTotal)
                .balance(dto.getBalance() != null ? dto.getBalance() : BigDecimal.ZERO)
                .customerId(dto.getCustomerId())
                .partner(dto.getPartner() != null ? dto.getPartner() : "Customer Refund")
                .contact(dto.getContact())
                .phone(dto.getPhone())
                .username(dto.getUsername() != null ? dto.getUsername() : "Admin")
                .status(dto.getStatus() != null ? dto.getStatus() : "NONE_VOID")
                .note(dto.getNote())
                .paymentType(dto.getPaymentType() != null ? dto.getPaymentType() : "Cash")
                .authorizationNote(dto.getAuthorizationNote())
                .invoices(new ArrayList<>())
                .build();

        if (dto.getInvoices() != null) {
            for (CustomerRefundInvoiceDto invDto : dto.getInvoices()) {
                CustomerRefundInvoice inv = CustomerRefundInvoice.builder()
                        .customerRefund(entity)
                        .code(invDto.getCode())
                        .date(invDto.getDate() != null ? invDto.getDate() : LocalDateTime.now())
                        .type(invDto.getType() != null ? invDto.getType() : "Return Invoice")
                        .amount(invDto.getAmount() != null ? invDto.getAmount() : BigDecimal.ZERO)
                        .balance(invDto.getBalance() != null ? invDto.getBalance() : BigDecimal.ZERO)
                        .payAmount(invDto.getPayAmount() != null ? invDto.getPayAmount() : BigDecimal.ZERO)
                        .payCurrency(invDto.getPayCurrency() != null ? invDto.getPayCurrency() : "USD")
                        .build();
                entity.getInvoices().add(inv);
            }
        }

        CustomerRefund saved = refundRepository.save(entity);
        return toDto(saved);
    }

    @Override
    public CustomerRefundDto updateRefund(Long id, CustomerRefundDto dto) {
        CustomerRefund entity = refundRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer Refund not found with id: " + id));

        if (dto.getPaymentDate() != null) entity.setPaymentDate(dto.getPaymentDate());
        if (dto.getRate() != null) entity.setRate(dto.getRate());
        if (dto.getPaidAmount() != null) entity.setPaidAmount(dto.getPaidAmount());
        if (dto.getBalance() != null) entity.setBalance(dto.getBalance());
        if (dto.getPartner() != null) entity.setPartner(dto.getPartner());
        if (dto.getContact() != null) entity.setContact(dto.getContact());
        if (dto.getPhone() != null) entity.setPhone(dto.getPhone());
        if (dto.getUsername() != null) entity.setUsername(dto.getUsername());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        if (dto.getNote() != null) entity.setNote(dto.getNote());
        if (dto.getPaymentType() != null) entity.setPaymentType(dto.getPaymentType());
        if (dto.getAuthorizationNote() != null) entity.setAuthorizationNote(dto.getAuthorizationNote());

        CustomerRefund saved = refundRepository.save(entity);
        return toDto(saved);
    }

    @Override
    public CustomerRefundDto updateStatus(Long id, String status) {
        CustomerRefund entity = refundRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer Refund not found with id: " + id));
        entity.setStatus(status != null ? status.toUpperCase() : "NONE_VOID");
        return toDto(refundRepository.save(entity));
    }

    @Override
    public void deleteRefund(Long id) {
        refundRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public String generateNextCode() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "CR-" + today + "-";
        long count = refundRepository.count() + 1;
        return String.format("%s%04d", prefix, count);
    }

    private CustomerRefundDto toDto(CustomerRefund entity) {
        List<CustomerRefundInvoiceDto> invoiceDtos = entity.getInvoices() != null
                ? entity.getInvoices().stream().map(this::toInvoiceDto).collect(Collectors.toList())
                : new ArrayList<>();

        return CustomerRefundDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .paymentDate(entity.getPaymentDate())
                .rate(entity.getRate())
                .paidAmount(entity.getPaidAmount())
                .balance(entity.getBalance())
                .customerId(entity.getCustomerId())
                .partner(entity.getPartner())
                .contact(entity.getContact())
                .phone(entity.getPhone())
                .username(entity.getUsername())
                .status(entity.getStatus())
                .note(entity.getNote())
                .paymentType(entity.getPaymentType())
                .authorizationNote(entity.getAuthorizationNote())
                .invoices(invoiceDtos)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private CustomerRefundInvoiceDto toInvoiceDto(CustomerRefundInvoice entity) {
        return CustomerRefundInvoiceDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .date(entity.getDate())
                .type(entity.getType())
                .amount(entity.getAmount())
                .balance(entity.getBalance())
                .payAmount(entity.getPayAmount())
                .payCurrency(entity.getPayCurrency())
                .build();
    }
}
