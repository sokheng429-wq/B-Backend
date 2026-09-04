package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.AgingInvoiceDto;

import java.util.List;
import java.util.Map;

public interface AgingInvoiceService {
    List<AgingInvoiceDto> getAllAgingInvoices(
            String search,
            String searchBy,
            String agingType,
            String salesperson,
            String customer,
            String customerGroup
    );

    AgingInvoiceDto getAgingInvoiceById(Long id);

    Map<String, Object> getAgingSummary();
}
