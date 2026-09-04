package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.PurchaseOrderDto;

import java.time.LocalDate;
import java.util.List;

public interface PurchaseOrderService {

    List<PurchaseOrderDto> getAllPurchaseOrders(
            String search,
            String searchBy,
            LocalDate fromDate,
            LocalDate toDate,
            String outlet,
            String purchasePerson,
            String status
    );

    PurchaseOrderDto getPurchaseOrderById(Long id);

    PurchaseOrderDto createPurchaseOrder(PurchaseOrderDto dto);

    PurchaseOrderDto updatePurchaseOrder(Long id, PurchaseOrderDto dto);

    PurchaseOrderDto updateStatus(Long id, String status);

    void deletePurchaseOrder(Long id);

    String generateNextCode();
}
