package com.bgroceries.backend.service.impl;

import com.bgroceries.backend.dto.QuotationDto;
import com.bgroceries.backend.dto.QuotationItemDto;
import com.bgroceries.backend.entity.Sale.Quotation;
import com.bgroceries.backend.entity.Sale.QuotationItem;
import com.bgroceries.backend.repository.QuotationRepository;
import com.bgroceries.backend.service.QuotationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuotationServiceImpl implements QuotationService {

    private final QuotationRepository quotationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<QuotationDto> getAllQuotations(
            String search,
            String searchBy,
            String status,
            String outlet,
            String customer,
            String salesperson,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        List<Quotation> list;

        if (searchBy != null && !searchBy.equalsIgnoreCase("any") && search != null && !search.isBlank()) {
            list = quotationRepository.searchByField(searchBy, search.trim());
        } else if ((search != null && !search.isBlank()) ||
                   (status != null && !status.equalsIgnoreCase("ALL")) ||
                   (outlet != null && !outlet.equalsIgnoreCase("ALL")) ||
                   (customer != null && !customer.isBlank()) ||
                   (salesperson != null && !salesperson.isBlank()) ||
                   startDate != null || endDate != null) {
            list = quotationRepository.searchQuotations(
                    search != null && !search.isBlank() ? search.trim() : null,
                    status != null && !status.equalsIgnoreCase("ALL") ? status : null,
                    outlet != null && !outlet.equalsIgnoreCase("ALL") ? outlet : null,
                    customer != null && !customer.isBlank() ? customer.trim() : null,
                    salesperson != null && !salesperson.isBlank() ? salesperson.trim() : null,
                    startDate,
                    endDate
            );
        } else {
            list = quotationRepository.findTop50ByOrderByCreatedAtDesc();
        }

        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public QuotationDto getQuotationById(Long id) {
        Quotation entity = quotationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Quotation not found with id: " + id));
        return toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public QuotationDto getQuotationByCode(String code) {
        Quotation entity = quotationRepository.findByCode(code)
                .orElseThrow(() -> new NoSuchElementException("Quotation not found with code: " + code));
        return toDto(entity);
    }

    @Override
    @Transactional
    public QuotationDto createQuotation(QuotationDto dto) {
        if (dto.getCode() == null || dto.getCode().isBlank() || dto.getCode().equalsIgnoreCase("AUTO")) {
            dto.setCode(generateNextCode());
        }

        Quotation entity = toEntity(dto);
        Quotation saved = quotationRepository.save(entity);
        log.info("Created Quotation: {} (ID: {})", saved.getCode(), saved.getId());
        return toDto(saved);
    }

    @Override
    @Transactional
    public QuotationDto updateQuotation(Long id, QuotationDto dto) {
        Quotation existing = quotationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Quotation not found with id: " + id));

        existing.setQuotationDate(dto.getQuotationDate() != null ? dto.getQuotationDate() : existing.getQuotationDate());
        existing.setExpiredDate(dto.getExpiredDate());
        existing.setCustomerId(dto.getCustomerId());
        existing.setCustomerName(dto.getCustomerName());
        existing.setCustomerPhone(dto.getCustomerPhone());
        existing.setCustomerAddress(dto.getCustomerAddress());
        existing.setSalesperson(dto.getSalesperson());
        existing.setPaymentTerm(dto.getPaymentTerm());
        existing.setOutlet(dto.getOutlet());
        existing.setTemplateName(dto.getTemplateName());
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            existing.setStatus(dto.getStatus());
        }
        existing.setSubAmount(dto.getSubAmount() != null ? dto.getSubAmount() : BigDecimal.ZERO);
        existing.setDiscountPercent(dto.getDiscountPercent() != null ? dto.getDiscountPercent() : BigDecimal.ZERO);
        existing.setDiscountAmount(dto.getDiscountAmount() != null ? dto.getDiscountAmount() : BigDecimal.ZERO);
        existing.setTaxAmount(dto.getTaxAmount() != null ? dto.getTaxAmount() : BigDecimal.ZERO);
        existing.setMarkupAmount(dto.getMarkupAmount() != null ? dto.getMarkupAmount() : BigDecimal.ZERO);
        existing.setGrandTotal(dto.getGrandTotal() != null ? dto.getGrandTotal() : BigDecimal.ZERO);
        existing.setBalance(dto.getBalance() != null ? dto.getBalance() : BigDecimal.ZERO);
        existing.setReference(dto.getReference());
        existing.setUsername(dto.getUsername());
        existing.setNote(dto.getNote());

        existing.setBillingName(dto.getBillingName());
        existing.setBillingPhone(dto.getBillingPhone());
        existing.setBillingEmail(dto.getBillingEmail());
        existing.setBillingAddress(dto.getBillingAddress());
        existing.setBillingCity(dto.getBillingCity());
        existing.setBillingTaxNo(dto.getBillingTaxNo());

        existing.setShippingName(dto.getShippingName());
        existing.setShippingPhone(dto.getShippingPhone());
        existing.setShippingAddress(dto.getShippingAddress());
        existing.setShippingCity(dto.getShippingCity());

        // Replace items
        if (dto.getItems() != null) {
            existing.getItems().clear();
            for (QuotationItemDto itemDto : dto.getItems()) {
                QuotationItem item = QuotationItem.builder()
                        .quotation(existing)
                        .productId(itemDto.getProductId())
                        .productCode(itemDto.getProductCode())
                        .barcode(itemDto.getBarcode())
                        .description(itemDto.getDescription() != null ? itemDto.getDescription() : "Item")
                        .qty(itemDto.getQty() != null ? itemDto.getQty() : BigDecimal.ONE)
                        .price(itemDto.getPrice() != null ? itemDto.getPrice() : BigDecimal.ZERO)
                        .discount(itemDto.getDiscount() != null ? itemDto.getDiscount() : BigDecimal.ZERO)
                        .uom(itemDto.getUom() != null ? itemDto.getUom() : "PCS")
                        .total(itemDto.getTotal() != null ? itemDto.getTotal() : BigDecimal.ZERO)
                        .note(itemDto.getNote())
                        .build();
                existing.getItems().add(item);
            }
        }

        Quotation updated = quotationRepository.save(existing);
        log.info("Updated Quotation: {}", updated.getCode());
        return toDto(updated);
    }

    @Override
    @Transactional
    public QuotationDto updateStatus(Long id, String status) {
        Quotation entity = quotationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Quotation not found with id: " + id));
        entity.setStatus(status.toUpperCase());
        return toDto(quotationRepository.save(entity));
    }

    @Override
    @Transactional
    public void deleteQuotation(Long id) {
        if (!quotationRepository.existsById(id)) {
            throw new NoSuchElementException("Quotation not found with id: " + id);
        }
        quotationRepository.deleteById(id);
        log.info("Deleted Quotation ID: {}", id);
    }

    @Override
    public String generateNextCode() {
        String prefix = "QUO-" + DateTimeFormatter.ofPattern("yyyyMM").format(LocalDateTime.now()) + "-";
        long count = quotationRepository.count() + 1;
        return String.format("%s%04d", prefix, count);
    }

    private QuotationDto toDto(Quotation entity) {
        List<QuotationItemDto> itemDtos = entity.getItems() != null
                ? entity.getItems().stream().map(i -> QuotationItemDto.builder()
                .id(i.getId())
                .productId(i.getProductId())
                .productCode(i.getProductCode())
                .barcode(i.getBarcode())
                .description(i.getDescription())
                .qty(i.getQty())
                .price(i.getPrice())
                .discount(i.getDiscount())
                .uom(i.getUom())
                .total(i.getTotal())
                .note(i.getNote())
                .build()).collect(Collectors.toList())
                : new ArrayList<>();

        return QuotationDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .quotationDate(entity.getQuotationDate())
                .expiredDate(entity.getExpiredDate())
                .customerId(entity.getCustomerId())
                .customerName(entity.getCustomerName())
                .customerPhone(entity.getCustomerPhone())
                .customerAddress(entity.getCustomerAddress())
                .salesperson(entity.getSalesperson())
                .paymentTerm(entity.getPaymentTerm())
                .outlet(entity.getOutlet())
                .templateName(entity.getTemplateName())
                .status(entity.getStatus())
                .subAmount(entity.getSubAmount())
                .discountPercent(entity.getDiscountPercent())
                .discountAmount(entity.getDiscountAmount())
                .taxAmount(entity.getTaxAmount())
                .markupAmount(entity.getMarkupAmount())
                .grandTotal(entity.getGrandTotal())
                .balance(entity.getBalance())
                .reference(entity.getReference())
                .username(entity.getUsername())
                .note(entity.getNote())
                .billingName(entity.getBillingName())
                .billingPhone(entity.getBillingPhone())
                .billingEmail(entity.getBillingEmail())
                .billingAddress(entity.getBillingAddress())
                .billingCity(entity.getBillingCity())
                .billingTaxNo(entity.getBillingTaxNo())
                .shippingName(entity.getShippingName())
                .shippingPhone(entity.getShippingPhone())
                .shippingAddress(entity.getShippingAddress())
                .shippingCity(entity.getShippingCity())
                .items(itemDtos)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private Quotation toEntity(QuotationDto dto) {
        Quotation entity = Quotation.builder()
                .code(dto.getCode())
                .quotationDate(dto.getQuotationDate() != null ? dto.getQuotationDate() : LocalDateTime.now())
                .expiredDate(dto.getExpiredDate() != null ? dto.getExpiredDate() : LocalDateTime.now().plusDays(7))
                .customerId(dto.getCustomerId())
                .customerName(dto.getCustomerName())
                .customerPhone(dto.getCustomerPhone())
                .customerAddress(dto.getCustomerAddress())
                .salesperson(dto.getSalesperson())
                .paymentTerm(dto.getPaymentTerm() != null ? dto.getPaymentTerm() : "Net 30")
                .outlet(dto.getOutlet() != null ? dto.getOutlet() : "Main Store")
                .templateName(dto.getTemplateName() != null ? dto.getTemplateName() : "Standard Quotation")
                .status(dto.getStatus() != null ? dto.getStatus() : "DRAFT")
                .subAmount(dto.getSubAmount() != null ? dto.getSubAmount() : BigDecimal.ZERO)
                .discountPercent(dto.getDiscountPercent() != null ? dto.getDiscountPercent() : BigDecimal.ZERO)
                .discountAmount(dto.getDiscountAmount() != null ? dto.getDiscountAmount() : BigDecimal.ZERO)
                .taxAmount(dto.getTaxAmount() != null ? dto.getTaxAmount() : BigDecimal.ZERO)
                .markupAmount(dto.getMarkupAmount() != null ? dto.getMarkupAmount() : BigDecimal.ZERO)
                .grandTotal(dto.getGrandTotal() != null ? dto.getGrandTotal() : BigDecimal.ZERO)
                .balance(dto.getBalance() != null ? dto.getBalance() : BigDecimal.ZERO)
                .reference(dto.getReference())
                .username(dto.getUsername() != null ? dto.getUsername() : "admin")
                .note(dto.getNote())
                .billingName(dto.getBillingName())
                .billingPhone(dto.getBillingPhone())
                .billingEmail(dto.getBillingEmail())
                .billingAddress(dto.getBillingAddress())
                .billingCity(dto.getBillingCity())
                .billingTaxNo(dto.getBillingTaxNo())
                .shippingName(dto.getShippingName())
                .shippingPhone(dto.getShippingPhone())
                .shippingAddress(dto.getShippingAddress())
                .shippingCity(dto.getShippingCity())
                .items(new ArrayList<>())
                .build();

        if (dto.getItems() != null) {
            for (QuotationItemDto itemDto : dto.getItems()) {
                QuotationItem item = QuotationItem.builder()
                        .quotation(entity)
                        .productId(itemDto.getProductId())
                        .productCode(itemDto.getProductCode())
                        .barcode(itemDto.getBarcode())
                        .description(itemDto.getDescription() != null ? itemDto.getDescription() : "Item")
                        .qty(itemDto.getQty() != null ? itemDto.getQty() : BigDecimal.ONE)
                        .price(itemDto.getPrice() != null ? itemDto.getPrice() : BigDecimal.ZERO)
                        .discount(itemDto.getDiscount() != null ? itemDto.getDiscount() : BigDecimal.ZERO)
                        .uom(itemDto.getUom() != null ? itemDto.getUom() : "PCS")
                        .total(itemDto.getTotal() != null ? itemDto.getTotal() : BigDecimal.ZERO)
                        .note(itemDto.getNote())
                        .build();
                entity.getItems().add(item);
            }
        }

        return entity;
    }
}
