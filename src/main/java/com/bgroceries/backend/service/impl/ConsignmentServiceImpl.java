package com.bgroceries.backend.service.impl;

import com.bgroceries.backend.dto.ConsignmentDto;
import com.bgroceries.backend.dto.ConsignmentItemDto;
import com.bgroceries.backend.entity.Sale.Consignment;
import com.bgroceries.backend.entity.Sale.ConsignmentItem;
import com.bgroceries.backend.repository.ConsignmentRepository;
import com.bgroceries.backend.service.ConsignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ConsignmentServiceImpl implements ConsignmentService {

    private final ConsignmentRepository consignmentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ConsignmentDto> getAllConsignments(
            String search,
            String searchBy,
            String status,
            String outlet,
            String customer,
            String salesperson,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        List<Consignment> list;

        boolean hasFilter = (search != null && !search.isBlank()) ||
                            (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) ||
                            (outlet != null && !outlet.isBlank() && !outlet.equalsIgnoreCase("ALL")) ||
                            (customer != null && !customer.isBlank() && !customer.equalsIgnoreCase("ALL")) ||
                            (salesperson != null && !salesperson.isBlank() && !salesperson.equalsIgnoreCase("ALL")) ||
                            startDate != null || endDate != null;

        if (!hasFilter) {
            list = consignmentRepository.findTop50ByOrderByCreatedAtDesc();
        } else if (search != null && !search.isBlank() && searchBy != null && !searchBy.equalsIgnoreCase("any")) {
            list = consignmentRepository.searchByField(searchBy.toLowerCase(), search.trim());
        } else {
            list = consignmentRepository.searchConsignments(
                    search != null ? search.trim() : null,
                    (status != null && !status.equalsIgnoreCase("ALL")) ? status.trim() : null,
                    (outlet != null && !outlet.equalsIgnoreCase("ALL")) ? outlet.trim() : null,
                    (customer != null && !customer.equalsIgnoreCase("ALL")) ? customer.trim() : null,
                    (salesperson != null && !salesperson.equalsIgnoreCase("ALL")) ? salesperson.trim() : null,
                    startDate,
                    endDate
            );
        }

        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ConsignmentDto getConsignmentById(Long id) {
        Consignment entity = consignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consignment not found with id: " + id));
        return toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public ConsignmentDto getConsignmentByCode(String code) {
        Consignment entity = consignmentRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Consignment not found with code: " + code));
        return toDto(entity);
    }

    @Override
    public ConsignmentDto createConsignment(ConsignmentDto dto) {
        String code = (dto.getCode() != null && !dto.getCode().isBlank() && !dto.getCode().equalsIgnoreCase("Auto Generate Code"))
                ? dto.getCode().trim()
                : generateNextCode();

        Consignment entity = Consignment.builder()
                .code(code)
                .consignmentDate(dto.getConsignmentDate() != null ? dto.getConsignmentDate() : LocalDateTime.now())
                .deliveryDate(dto.getDeliveryDate() != null ? dto.getDeliveryDate() : LocalDateTime.now())
                .customerId(dto.getCustomerId())
                .customerName(dto.getCustomerName())
                .customerPhone(dto.getCustomerPhone())
                .customerAddress(dto.getCustomerAddress())
                .salesperson(dto.getSalesperson())
                .paymentTerm(dto.getPaymentTerm() != null ? dto.getPaymentTerm() : "Cash")
                .outlet(dto.getOutlet() != null ? dto.getOutlet() : "Main Store")
                .templateName(dto.getTemplateName() != null ? dto.getTemplateName() : "Standard Consignment")
                .status(dto.getStatus() != null ? dto.getStatus() : "OPEN")
                .reference(dto.getReference())
                .username(dto.getUsername() != null ? dto.getUsername() : "Admin")
                .note(dto.getNote())
                .billingName(dto.getBillingName())
                .billingPhone(dto.getBillingPhone())
                .billingEmail(dto.getBillingEmail())
                .billingAddress(dto.getBillingAddress())
                .shippingRecipient(dto.getShippingRecipient())
                .shippingPhone(dto.getShippingPhone())
                .shippingAddress(dto.getShippingAddress())
                .shippingCourier(dto.getShippingCourier())
                .items(new ArrayList<>())
                .build();

        recalculateTotals(entity, dto);

        Consignment saved = consignmentRepository.save(entity);
        return toDto(saved);
    }

    @Override
    public ConsignmentDto updateConsignment(Long id, ConsignmentDto dto) {
        Consignment entity = consignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consignment not found with id: " + id));

        if (dto.getConsignmentDate() != null) entity.setConsignmentDate(dto.getConsignmentDate());
        if (dto.getDeliveryDate() != null) entity.setDeliveryDate(dto.getDeliveryDate());
        if (dto.getCustomerId() != null) entity.setCustomerId(dto.getCustomerId());
        if (dto.getCustomerName() != null) entity.setCustomerName(dto.getCustomerName());
        if (dto.getCustomerPhone() != null) entity.setCustomerPhone(dto.getCustomerPhone());
        if (dto.getCustomerAddress() != null) entity.setCustomerAddress(dto.getCustomerAddress());
        if (dto.getSalesperson() != null) entity.setSalesperson(dto.getSalesperson());
        if (dto.getPaymentTerm() != null) entity.setPaymentTerm(dto.getPaymentTerm());
        if (dto.getOutlet() != null) entity.setOutlet(dto.getOutlet());
        if (dto.getTemplateName() != null) entity.setTemplateName(dto.getTemplateName());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        if (dto.getReference() != null) entity.setReference(dto.getReference());
        if (dto.getNote() != null) entity.setNote(dto.getNote());
        if (dto.getBillingName() != null) entity.setBillingName(dto.getBillingName());
        if (dto.getBillingPhone() != null) entity.setBillingPhone(dto.getBillingPhone());
        if (dto.getBillingEmail() != null) entity.setBillingEmail(dto.getBillingEmail());
        if (dto.getBillingAddress() != null) entity.setBillingAddress(dto.getBillingAddress());
        if (dto.getShippingRecipient() != null) entity.setShippingRecipient(dto.getShippingRecipient());
        if (dto.getShippingPhone() != null) entity.setShippingPhone(dto.getShippingPhone());
        if (dto.getShippingAddress() != null) entity.setShippingAddress(dto.getShippingAddress());
        if (dto.getShippingCourier() != null) entity.setShippingCourier(dto.getShippingCourier());

        entity.getItems().clear();
        recalculateTotals(entity, dto);

        Consignment saved = consignmentRepository.save(entity);
        return toDto(saved);
    }

    @Override
    public ConsignmentDto updateStatus(Long id, String status) {
        Consignment entity = consignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consignment not found with id: " + id));
        entity.setStatus(status != null ? status.toUpperCase() : "OPEN");
        return toDto(consignmentRepository.save(entity));
    }

    @Override
    public void deleteConsignment(Long id) {
        consignmentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public String generateNextCode() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "CSG-" + today + "-";
        long count = consignmentRepository.count() + 1;
        return String.format("%s%04d", prefix, count);
    }

    private void recalculateTotals(Consignment entity, ConsignmentDto dto) {
        BigDecimal subAmount = BigDecimal.ZERO;

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (ConsignmentItemDto itemDto : dto.getItems()) {
                BigDecimal qty = itemDto.getQty() != null ? itemDto.getQty() : BigDecimal.ONE;
                BigDecimal price = itemDto.getPrice() != null ? itemDto.getPrice() : BigDecimal.ZERO;
                BigDecimal discount = itemDto.getDiscount() != null ? itemDto.getDiscount() : BigDecimal.ZERO;

                BigDecimal lineTotal = qty.multiply(price).subtract(discount);
                if (lineTotal.compareTo(BigDecimal.ZERO) < 0) lineTotal = BigDecimal.ZERO;

                ConsignmentItem item = ConsignmentItem.builder()
                        .productId(itemDto.getProductId())
                        .productCode(itemDto.getProductCode())
                        .barcode(itemDto.getBarcode())
                        .description(itemDto.getDescription() != null ? itemDto.getDescription() : "Item")
                        .qty(qty)
                        .price(price)
                        .discount(discount)
                        .uom(itemDto.getUom() != null ? itemDto.getUom() : "PCS")
                        .total(lineTotal.setScale(2, RoundingMode.HALF_UP))
                        .note(itemDto.getNote())
                        .build();

                entity.addItem(item);
                subAmount = subAmount.add(lineTotal);
            }
        }

        BigDecimal discountPercent = dto.getDiscountPercent() != null ? dto.getDiscountPercent() : BigDecimal.ZERO;
        BigDecimal discountAmount = dto.getDiscountAmount() != null ? dto.getDiscountAmount() : BigDecimal.ZERO;

        if (discountPercent.compareTo(BigDecimal.ZERO) > 0 && discountAmount.compareTo(BigDecimal.ZERO) == 0) {
            discountAmount = subAmount.multiply(discountPercent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        BigDecimal taxAmount = dto.getTaxAmount() != null ? dto.getTaxAmount() : BigDecimal.ZERO;

        BigDecimal grandTotal = subAmount.subtract(discountAmount).add(taxAmount);
        if (grandTotal.compareTo(BigDecimal.ZERO) < 0) grandTotal = BigDecimal.ZERO;

        BigDecimal balance = dto.getBalance() != null ? dto.getBalance() : grandTotal;

        entity.setSubAmount(subAmount.setScale(2, RoundingMode.HALF_UP));
        entity.setDiscountPercent(discountPercent);
        entity.setDiscountAmount(discountAmount.setScale(2, RoundingMode.HALF_UP));
        entity.setTaxAmount(taxAmount.setScale(2, RoundingMode.HALF_UP));
        entity.setGrandTotal(grandTotal.setScale(2, RoundingMode.HALF_UP));
        entity.setBalance(balance.setScale(2, RoundingMode.HALF_UP));
    }

    private ConsignmentDto toDto(Consignment entity) {
        List<ConsignmentItemDto> itemDtos = entity.getItems().stream()
                .map(i -> ConsignmentItemDto.builder()
                        .id(i.getId())
                        .consignmentId(entity.getId())
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
                        .build())
                .collect(Collectors.toList());

        return ConsignmentDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .consignmentDate(entity.getConsignmentDate())
                .deliveryDate(entity.getDeliveryDate())
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
                .grandTotal(entity.getGrandTotal())
                .balance(entity.getBalance())
                .reference(entity.getReference())
                .username(entity.getUsername())
                .note(entity.getNote())
                .billingName(entity.getBillingName())
                .billingPhone(entity.getBillingPhone())
                .billingEmail(entity.getBillingEmail())
                .billingAddress(entity.getBillingAddress())
                .shippingRecipient(entity.getShippingRecipient())
                .shippingPhone(entity.getShippingPhone())
                .shippingAddress(entity.getShippingAddress())
                .shippingCourier(entity.getShippingCourier())
                .items(itemDtos)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
