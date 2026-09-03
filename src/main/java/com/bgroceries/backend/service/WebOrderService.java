package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.WebOrderDto;
import java.time.LocalDateTime;
import java.util.List;

public interface WebOrderService {
    List<WebOrderDto> getAllWebOrders(String search, String searchBy, String status, String outlet, LocalDateTime startDate, LocalDateTime endDate);
    WebOrderDto getWebOrderById(Long id);
    WebOrderDto createWebOrder(WebOrderDto dto);
    WebOrderDto updateWebOrder(Long id, WebOrderDto dto);
    void deleteWebOrder(Long id);
    String generateNextCode();
}
