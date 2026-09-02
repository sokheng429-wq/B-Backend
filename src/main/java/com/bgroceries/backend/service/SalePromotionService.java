package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.SalePromotionDto;
import java.util.List;

public interface SalePromotionService {
    List<SalePromotionDto> getAll(String search, String searchBy, Boolean activeOnly);
    SalePromotionDto getById(Long id);
    SalePromotionDto getByCode(String code);
    SalePromotionDto create(SalePromotionDto dto);
    SalePromotionDto update(Long id, SalePromotionDto dto);
    SalePromotionDto toggleActive(Long id);
    void delete(Long id);
    String getNextPromoCode();
}