package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.SaleOrderDto;
import java.time.LocalDateTime;
import java.util.List;

public interface SaleOrderService {
    List<SaleOrderDto> getAllSaleOrders(String search, String searchBy, String status, String outlet, LocalDateTime startDate, LocalDateTime endDate);
    SaleOrderDto getSaleOrderById(Long id);
    SaleOrderDto createSaleOrder(SaleOrderDto dto);
    SaleOrderDto updateSaleOrder(Long id, SaleOrderDto dto);
    void deleteSaleOrder(Long id);
    String generateNextCode();
}
