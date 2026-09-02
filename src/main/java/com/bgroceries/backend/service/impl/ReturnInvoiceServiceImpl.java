package com.bgroceries.backend.service.impl;

import com.bgroceries.backend.dto.ReturnInvoiceDto;
import com.bgroceries.backend.dto.ReturnInvoiceItemDto;
import com.bgroceries.backend.entity.Sale.ReturnInvoice;
import com.bgroceries.backend.entity.Sale.ReturnInvoiceItem;
import com.bgroceries.backend.repository.ReturnInvoiceRepository;
import com.bgroceries.backend.service.ReturnInvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReturnInvoiceServiceImpl implements ReturnInvoiceService {

    private final ReturnInvoiceRepository returnInvoiceRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ReturnInvoiceDto> getAll(String search, String searchBy, String outlet, LocalDate startDate, LocalDate endDate) {
        List<ReturnInvoice> list;

        if (search != null && !search.trim().isEmpty() && searchBy != null && !searchBy.equalsIgnoreCase("any")) {
            list = returnInvoiceRepository.searchByField(searchBy, search.trim());
        } else if ((search != null && !search.trim().isEmpty()) || (outlet != null && !outlet.isEmpty() && !outlet.equalsIgnoreCase("all")) || startDate != null || endDate != null) {
            list = returnInvoiceRepository.searchWithFilters(
                    search != null ? search.trim() : "",
                    outlet != null ? outlet.trim() : "",
                    startDate,
                    endDate
            );
        } else {
            list = returnInvoiceRepository.findAll();
            list.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        }

        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnInvoiceDto getById(Long id) {
        ReturnInvoice entity = returnInvoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Return invoice not found with id: " + id));
        return toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnInvoiceDto getByCode(String code) {
        ReturnInvoice entity = returnInvoiceRepository.findByInvoiceCode(code)
                .orElseThrow(() -> new RuntimeException("Return invoice not found with code: " + code));
        return toDto(entity);
    }

    @Override
    @Transactional
    public ReturnInvoiceDto create(ReturnInvoiceDto dto) {
        String code = dto.getInvoiceCode();
        if (code == null || code.trim().isEmpty() || returnInvoiceRepository.existsByInvoiceCode(code)) {
            code = getNextReturnCode();
        }

        ReturnInvoice entity = ReturnInvoice.builder()
                .invoiceCode(code)
                .applyToInvoice(dto.getApplyToInvoice())
                .returnDate(dto.getReturnDate() != null ? dto.getReturnDate() : LocalDate.now())
                .customerId(dto.getCustomerId())
                .customerName(dto.getCustomerName())
                .customerPhone(dto.getCustomerPhone())
                .customerAddress(dto.getCustomerAddress())
                .grandTotal(dto.getGrandTotal() != null ? dto.getGrandTotal() : BigDecimal.ZERO)
                .balance(dto.getBalance() != null ? dto.getBalance() : BigDecimal.ZERO)
                .taxCode(dto.getTaxCode())
                .paymentTerm(dto.getPaymentTerm())
                .salesperson(dto.getSalesperson())
                .markup(dto.getMarkup() != null ? dto.getMarkup() : BigDecimal.ZERO)
                .outlet(dto.getOutlet())
                .username(dto.getUsername())
                .soCode(dto.getSoCode())
                .status(dto.getStatus() != null ? dto.getStatus() : "RETURNED")
                .reason(dto.getReason())
                .build();

        if (dto.getLines() != null && !dto.getLines().isEmpty()) {
            for (ReturnInvoiceItemDto itemDto : dto.getLines()) {
                ReturnInvoiceItem item = ReturnInvoiceItem.builder()
                        .returnInvoice(entity)
                        .productId(itemDto.getProductId())
                        .productCode(itemDto.getProductCode())
                        .description(itemDto.getDescription() != null ? itemDto.getDescription() : "Item")
                        .qty(itemDto.getQty() != null ? itemDto.getQty() : BigDecimal.ONE)
                        .unitPrice(itemDto.getUnitPrice() != null ? itemDto.getUnitPrice() : BigDecimal.ZERO)
                        .discount(itemDto.getDiscount() != null ? itemDto.getDiscount() : BigDecimal.ZERO)
                        .uom(itemDto.getUom() != null ? itemDto.getUom() : "Pcs")
                        .totalPrice(itemDto.getTotalPrice() != null ? itemDto.getTotalPrice() : BigDecimal.ZERO)
                        .build();
                entity.getLines().add(item);
            }
        }

        ReturnInvoice saved = returnInvoiceRepository.save(entity);
        return toDto(saved);
    }

    @Override
    @Transactional
    public ReturnInvoiceDto update(Long id, ReturnInvoiceDto dto) {
        ReturnInvoice entity = returnInvoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Return invoice not found with id: " + id));

        entity.setApplyToInvoice(dto.getApplyToInvoice());
        if (dto.getReturnDate() != null) entity.setReturnDate(dto.getReturnDate());
        entity.setCustomerName(dto.getCustomerName());
        entity.setCustomerPhone(dto.getCustomerPhone());
        entity.setCustomerAddress(dto.getCustomerAddress());
        if (dto.getGrandTotal() != null) entity.setGrandTotal(dto.getGrandTotal());
        if (dto.getBalance() != null) entity.setBalance(dto.getBalance());
        entity.setTaxCode(dto.getTaxCode());
        entity.setPaymentTerm(dto.getPaymentTerm());
        entity.setSalesperson(dto.getSalesperson());
        if (dto.getMarkup() != null) entity.setMarkup(dto.getMarkup());
        entity.setOutlet(dto.getOutlet());
        entity.setSoCode(dto.getSoCode());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        entity.setReason(dto.getReason());

        ReturnInvoice saved = returnInvoiceRepository.save(entity);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        returnInvoiceRepository.deleteById(id);
    }

    @Override
    public String getNextReturnCode() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        long count = returnInvoiceRepository.count() + 1;
        String seq = String.format("%04d", count);
        String code = "RET-" + datePart + "-" + seq;
        while (returnInvoiceRepository.existsByInvoiceCode(code)) {
            count++;
            seq = String.format("%04d", count);
            code = "RET-" + datePart + "-" + seq;
        }
        return code;
    }

    private ReturnInvoiceDto toDto(ReturnInvoice entity) {
        List<ReturnInvoiceItemDto> itemDtos = new ArrayList<>();
        if (entity.getLines() != null) {
            for (ReturnInvoiceItem item : entity.getLines()) {
                itemDtos.add(ReturnInvoiceItemDto.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productCode(item.getProductCode())
                        .description(item.getDescription())
                        .qty(item.getQty())
                        .unitPrice(item.getUnitPrice())
                        .discount(item.getDiscount())
                        .uom(item.getUom())
                        .totalPrice(item.getTotalPrice())
                        .build());
            }
        }

        return ReturnInvoiceDto.builder()
                .id(entity.getId())
                .invoiceCode(entity.getInvoiceCode())
                .applyToInvoice(entity.getApplyToInvoice())
                .returnDate(entity.getReturnDate())
                .customerId(entity.getCustomerId())
                .customerName(entity.getCustomerName())
                .customerPhone(entity.getCustomerPhone())
                .customerAddress(entity.getCustomerAddress())
                .grandTotal(entity.getGrandTotal())
                .balance(entity.getBalance())
                .taxCode(entity.getTaxCode())
                .paymentTerm(entity.getPaymentTerm())
                .salesperson(entity.getSalesperson())
                .markup(entity.getMarkup())
                .outlet(entity.getOutlet())
                .username(entity.getUsername())
                .soCode(entity.getSoCode())
                .status(entity.getStatus())
                .reason(entity.getReason())
                .lines(itemDtos)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}