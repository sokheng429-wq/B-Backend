package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.SaleInvoiceDto;
import com.bgroceries.backend.dto.SaleInvoicePaymentDto;
import com.bgroceries.backend.dto.SaleInvoiceStatsDto;

import java.time.LocalDate;
import java.util.List;

public interface SaleInvoiceService {

    List<SaleInvoiceDto> getAllInvoices(String search, String searchBy, String status, LocalDate startDate, LocalDate endDate);

    SaleInvoiceDto getInvoiceById(Long id);

    SaleInvoiceDto getInvoiceByCode(String code);

    SaleInvoiceDto createInvoice(SaleInvoiceDto dto);

    SaleInvoiceDto updateInvoice(Long id, SaleInvoiceDto dto);

    void deleteInvoice(Long id);

    SaleInvoiceDto recordPayment(Long id, SaleInvoicePaymentDto paymentDto);

    String generateNextInvoiceCode();

    SaleInvoiceStatsDto getStats();
}