package com.bgroceries.backend.service.impl;

import com.bgroceries.backend.dto.SaleOrderDto;
import com.bgroceries.backend.dto.SaleOrderItemDto;
import com.bgroceries.backend.entity.Sale.SaleOrder;
import com.bgroceries.backend.entity.Sale.SaleOrderItem;
import com.bgroceries.backend.repository.SaleOrderRepository;
import com.bgroceries.backend.service.SaleOrderService;
import lombok.RequiredArgsConstructor;
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
public class SaleOrderServiceImpl implements SaleOrderService {
    private final SaleOrderRepository saleOrderRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SaleOrderDto> getAllSaleOrders(String search, String searchBy, String status, String outlet, LocalDateTime startDate, LocalDateTime endDate) {
        List<SaleOrder> list;
        if ((search != null && !search.isBlank()) || (status != null && !status.equalsIgnoreCase("ALL")) || (outlet != null && !outlet.equalsIgnoreCase("ALL")) || startDate != null || endDate != null) {
            list = saleOrderRepository.searchSaleOrders(search, status, outlet, startDate, endDate);
        } else {
            list = saleOrderRepository.findTop50ByOrderByCreatedAtDesc();
        }
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SaleOrderDto getSaleOrderById(Long id) {
        return toDto(saleOrderRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Sale Order not found: " + id)));
    }

    @Override
    @Transactional
    public SaleOrderDto createSaleOrder(SaleOrderDto dto) {
        if (dto.getCode() == null || dto.getCode().isBlank() || dto.getCode().equalsIgnoreCase("AUTO")) {
            dto.setCode(generateNextCode());
        }
        SaleOrder entity = toEntity(dto);
        return toDto(saleOrderRepository.save(entity));
    }

    @Override
    @Transactional
    public SaleOrderDto updateSaleOrder(Long id, SaleOrderDto dto) {
        SaleOrder existing = saleOrderRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Sale Order not found: " + id));
        existing.setQuoteCode(dto.getQuoteCode());
        existing.setPoCode(dto.getPoCode());
        existing.setDeliveryDate(dto.getDeliveryDate());
        existing.setCustomerName(dto.getCustomerName());
        existing.setCustomerPhone(dto.getCustomerPhone());
        existing.setSalesperson(dto.getSalesperson());
        existing.setPaymentTerm(dto.getPaymentTerm());
        existing.setOutlet(dto.getOutlet());
        existing.setStatus(dto.getStatus() != null ? dto.getStatus() : existing.getStatus());
        existing.setGrandTotal(dto.getGrandTotal() != null ? dto.getGrandTotal() : BigDecimal.ZERO);
        existing.setBalance(dto.getBalance() != null ? dto.getBalance() : BigDecimal.ZERO);
        existing.setMarkupAmount(dto.getMarkupAmount() != null ? dto.getMarkupAmount() : BigDecimal.ZERO);
        existing.setReference(dto.getReference());
        existing.setUsername(dto.getUsername());
        existing.setNote(dto.getNote());

        if (dto.getItems() != null) {
            existing.getItems().clear();
            for (SaleOrderItemDto it : dto.getItems()) {
                existing.getItems().add(SaleOrderItem.builder()
                        .saleOrder(existing)
                        .productId(it.getProductId())
                        .productCode(it.getProductCode())
                        .barcode(it.getBarcode())
                        .description(it.getDescription())
                        .qty(it.getQty() != null ? it.getQty() : BigDecimal.ONE)
                        .price(it.getPrice() != null ? it.getPrice() : BigDecimal.ZERO)
                        .discount(it.getDiscount() != null ? it.getDiscount() : BigDecimal.ZERO)
                        .uom(it.getUom() != null ? it.getUom() : "PCS")
                        .total(it.getTotal() != null ? it.getTotal() : BigDecimal.ZERO)
                        .build());
            }
        }
        return toDto(saleOrderRepository.save(existing));
    }

    @Override
    @Transactional
    public void deleteSaleOrder(Long id) {
        saleOrderRepository.deleteById(id);
    }

    @Override
    public String generateNextCode() {
        return "SO-" + DateTimeFormatter.ofPattern("yyyyMM").format(LocalDateTime.now()) + "-" + String.format("%04d", saleOrderRepository.count() + 1);
    }

    private SaleOrderDto toDto(SaleOrder entity) {
        List<SaleOrderItemDto> itemDtos = entity.getItems() != null
                ? entity.getItems().stream().map(i -> SaleOrderItemDto.builder()
                .id(i.getId()).productId(i.getProductId()).productCode(i.getProductCode()).barcode(i.getBarcode()).description(i.getDescription()).qty(i.getQty()).price(i.getPrice()).discount(i.getDiscount()).uom(i.getUom()).total(i.getTotal()).build()).collect(Collectors.toList())
                : new ArrayList<>();

        return SaleOrderDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .quoteCode(entity.getQuoteCode())
                .poCode(entity.getPoCode())
                .orderDate(entity.getOrderDate())
                .deliveryDate(entity.getDeliveryDate())
                .customerId(entity.getCustomerId())
                .customerName(entity.getCustomerName())
                .customerPhone(entity.getCustomerPhone())
                .salesperson(entity.getSalesperson())
                .paymentTerm(entity.getPaymentTerm())
                .outlet(entity.getOutlet())
                .templateName(entity.getTemplateName())
                .status(entity.getStatus())
                .creditLimit(entity.getCreditLimit())
                .availableCredit(entity.getAvailableCredit())
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
                .relatedPurchaseOrder(entity.getRelatedPurchaseOrder())
                .items(itemDtos)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private SaleOrder toEntity(SaleOrderDto dto) {
        SaleOrder entity = SaleOrder.builder()
                .code(dto.getCode())
                .quoteCode(dto.getQuoteCode())
                .poCode(dto.getPoCode())
                .orderDate(dto.getOrderDate() != null ? dto.getOrderDate() : LocalDateTime.now())
                .deliveryDate(dto.getDeliveryDate() != null ? dto.getDeliveryDate() : LocalDateTime.now().plusDays(3))
                .customerId(dto.getCustomerId())
                .customerName(dto.getCustomerName())
                .customerPhone(dto.getCustomerPhone())
                .salesperson(dto.getSalesperson())
                .paymentTerm(dto.getPaymentTerm())
                .outlet(dto.getOutlet())
                .templateName(dto.getTemplateName())
                .status(dto.getStatus() != null ? dto.getStatus() : "CONFIRMED")
                .creditLimit(dto.getCreditLimit() != null ? dto.getCreditLimit() : BigDecimal.ZERO)
                .availableCredit(dto.getAvailableCredit() != null ? dto.getAvailableCredit() : BigDecimal.ZERO)
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
                .relatedPurchaseOrder(dto.getRelatedPurchaseOrder())
                .items(new ArrayList<>())
                .build();

        if (dto.getItems() != null) {
            for (SaleOrderItemDto it : dto.getItems()) {
                entity.getItems().add(SaleOrderItem.builder()
                        .saleOrder(entity)
                        .productId(it.getProductId())
                        .productCode(it.getProductCode())
                        .barcode(it.getBarcode())
                        .description(it.getDescription())
                        .qty(it.getQty() != null ? it.getQty() : BigDecimal.ONE)
                        .price(it.getPrice() != null ? it.getPrice() : BigDecimal.ZERO)
                        .discount(it.getDiscount() != null ? it.getDiscount() : BigDecimal.ZERO)
                        .uom(it.getUom() != null ? it.getUom() : "PCS")
                        .total(it.getTotal() != null ? it.getTotal() : BigDecimal.ZERO)
                        .build());
            }
        }
        return entity;
    }
}
