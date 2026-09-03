package com.bgroceries.backend.service.impl;

import com.bgroceries.backend.dto.WebOrderDto;
import com.bgroceries.backend.dto.WebOrderItemDto;
import com.bgroceries.backend.entity.Sale.WebOrder;
import com.bgroceries.backend.entity.Sale.WebOrderItem;
import com.bgroceries.backend.repository.WebOrderRepository;
import com.bgroceries.backend.service.WebOrderService;
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
public class WebOrderServiceImpl implements WebOrderService {
    private final WebOrderRepository webOrderRepository;

    @Override
    @Transactional(readOnly = true)
    public List<WebOrderDto> getAllWebOrders(String search, String searchBy, String status, String outlet, LocalDateTime startDate, LocalDateTime endDate) {
        List<WebOrder> list;
        if ((search != null && !search.isBlank()) || (status != null && !status.equalsIgnoreCase("ALL")) || (outlet != null && !outlet.equalsIgnoreCase("ALL")) || startDate != null || endDate != null) {
            list = webOrderRepository.searchWebOrders(search, status, outlet, startDate, endDate);
        } else {
            list = webOrderRepository.findTop50ByOrderByCreatedAtDesc();
        }
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WebOrderDto getWebOrderById(Long id) {
        return toDto(webOrderRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Web Order not found: " + id)));
    }

    @Override
    @Transactional
    public WebOrderDto createWebOrder(WebOrderDto dto) {
        if (dto.getCode() == null || dto.getCode().isBlank() || dto.getCode().equalsIgnoreCase("AUTO")) {
            dto.setCode(generateNextCode());
        }
        WebOrder entity = toEntity(dto);
        return toDto(webOrderRepository.save(entity));
    }

    @Override
    @Transactional
    public WebOrderDto updateWebOrder(Long id, WebOrderDto dto) {
        WebOrder existing = webOrderRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Web Order not found: " + id));
        existing.setDeliveryDate(dto.getDeliveryDate());
        existing.setCustomerName(dto.getCustomerName());
        existing.setPhone(dto.getPhone());
        existing.setSalesperson(dto.getSalesperson());
        existing.setOutlet(dto.getOutlet());
        existing.setStatus(dto.getStatus() != null ? dto.getStatus() : existing.getStatus());
        existing.setGrandTotal(dto.getGrandTotal() != null ? dto.getGrandTotal() : BigDecimal.ZERO);
        existing.setBalance(dto.getBalance() != null ? dto.getBalance() : BigDecimal.ZERO);
        existing.setMarkup(dto.getMarkup() != null ? dto.getMarkup() : BigDecimal.ZERO);
        existing.setReference(dto.getReference());
        existing.setUsername(dto.getUsername());
        existing.setShippingAddress(dto.getShippingAddress());
        return toDto(webOrderRepository.save(existing));
    }

    @Override
    @Transactional
    public void deleteWebOrder(Long id) {
        webOrderRepository.deleteById(id);
    }

    @Override
    public String generateNextCode() {
        return "WEB-" + DateTimeFormatter.ofPattern("yyyyMM").format(LocalDateTime.now()) + "-" + String.format("%04d", webOrderRepository.count() + 1);
    }

    private WebOrderDto toDto(WebOrder entity) {
        List<WebOrderItemDto> items = entity.getItems() != null
                ? entity.getItems().stream().map(i -> WebOrderItemDto.builder()
                .id(i.getId()).productId(i.getProductId()).productCode(i.getProductCode()).description(i.getDescription()).qty(i.getQty()).price(i.getPrice()).total(i.getTotal()).build()).collect(Collectors.toList())
                : new ArrayList<>();

        return WebOrderDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .orderDate(entity.getOrderDate())
                .deliveryDate(entity.getDeliveryDate())
                .salesperson(entity.getSalesperson())
                .customerName(entity.getCustomerName())
                .phone(entity.getPhone())
                .grandTotal(entity.getGrandTotal())
                .balance(entity.getBalance())
                .status(entity.getStatus())
                .reference(entity.getReference())
                .username(entity.getUsername())
                .markup(entity.getMarkup())
                .outlet(entity.getOutlet())
                .channel(entity.getChannel())
                .shippingAddress(entity.getShippingAddress())
                .items(items)
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private WebOrder toEntity(WebOrderDto dto) {
        WebOrder entity = WebOrder.builder()
                .code(dto.getCode())
                .orderDate(dto.getOrderDate() != null ? dto.getOrderDate() : LocalDateTime.now())
                .deliveryDate(dto.getDeliveryDate() != null ? dto.getDeliveryDate() : LocalDateTime.now().plusDays(2))
                .salesperson(dto.getSalesperson())
                .customerName(dto.getCustomerName())
                .phone(dto.getPhone())
                .grandTotal(dto.getGrandTotal() != null ? dto.getGrandTotal() : BigDecimal.ZERO)
                .balance(dto.getBalance() != null ? dto.getBalance() : BigDecimal.ZERO)
                .status(dto.getStatus() != null ? dto.getStatus() : "PENDING")
                .reference(dto.getReference())
                .username(dto.getUsername() != null ? dto.getUsername() : "online_buyer")
                .markup(dto.getMarkup() != null ? dto.getMarkup() : BigDecimal.ZERO)
                .outlet(dto.getOutlet() != null ? dto.getOutlet() : "Online Store")
                .channel(dto.getChannel() != null ? dto.getChannel() : "Web Storefront")
                .shippingAddress(dto.getShippingAddress())
                .items(new ArrayList<>())
                .build();

        if (dto.getItems() != null) {
            for (WebOrderItemDto it : dto.getItems()) {
                entity.getItems().add(WebOrderItem.builder()
                        .webOrder(entity)
                        .productId(it.getProductId())
                        .productCode(it.getProductCode())
                        .description(it.getDescription())
                        .qty(it.getQty() != null ? it.getQty() : BigDecimal.ONE)
                        .price(it.getPrice() != null ? it.getPrice() : BigDecimal.ZERO)
                        .total(it.getTotal() != null ? it.getTotal() : BigDecimal.ZERO)
                        .build());
            }
        }
        return entity;
    }
}
