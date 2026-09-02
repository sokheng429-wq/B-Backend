package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.ReturnInvoiceDto;
import java.time.LocalDate;
import java.util.List;

public interface ReturnInvoiceService {
    List<ReturnInvoiceDto> getAll(String search, String searchBy, String outlet, LocalDate startDate, LocalDate endDate);
    ReturnInvoiceDto getById(Long id);
    ReturnInvoiceDto getByCode(String code);
    ReturnInvoiceDto create(ReturnInvoiceDto dto);
    ReturnInvoiceDto update(Long id, ReturnInvoiceDto dto);
    void delete(Long id);
    String getNextReturnCode();
}