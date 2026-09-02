package com.bgroceries.backend.service.impl;

import com.bgroceries.backend.dto.SalePromotionDto;
import com.bgroceries.backend.entity.Sale.SalePromotion;
import com.bgroceries.backend.repository.SalePromotionRepository;
import com.bgroceries.backend.service.SalePromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalePromotionServiceImpl implements SalePromotionService {

    private final SalePromotionRepository promoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SalePromotionDto> getAll(String search, String searchBy, Boolean activeOnly) {
        List<SalePromotion> list;

        if (search != null && !search.trim().isEmpty()) {
            String by = (searchBy != null && !searchBy.isEmpty()) ? searchBy : "any";
            list = promoRepository.searchByField(by, search.trim());
        } else if (Boolean.TRUE.equals(activeOnly)) {
            list = promoRepository.findByActiveTrueOrderByCreatedAtDesc();
        } else {
            list = promoRepository.findAll();
            list.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        }

        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SalePromotionDto getById(Long id) {
        SalePromotion entity = promoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));
        return toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public SalePromotionDto getByCode(String code) {
        SalePromotion entity = promoRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Promotion not found with code: " + code));
        return toDto(entity);
    }

    @Override
    @Transactional
    public SalePromotionDto create(SalePromotionDto dto) {
        String code = dto.getCode();
        if (code == null || code.trim().isEmpty() || promoRepository.existsByCode(code)) {
            code = getNextPromoCode();
        }

        SalePromotion entity = SalePromotion.builder()
                .code(code)
                .description(dto.getDescription())
                .secondLanguage(dto.getSecondLanguage())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .priceBook(dto.getPriceBook())
                .discountType(dto.getDiscountType() != null ? dto.getDiscountType() : "PERCENTAGE")
                .minRequirementType(dto.getMinRequirementType() != null ? dto.getMinRequirementType() : "ENTIRE_ORDER")
                .minRequirementValue(dto.getMinRequirementValue() != null ? dto.getMinRequirementValue() : BigDecimal.ZERO)
                .discountValueScope(dto.getDiscountValueScope() != null ? dto.getDiscountValueScope() : "ENTIRE_ORDER")
                .targetScopeId(dto.getTargetScopeId())
                .targetScopeName(dto.getTargetScopeName())
                .discountValue(dto.getDiscountValue() != null ? dto.getDiscountValue() : BigDecimal.ZERO)
                .dateType(dto.getDateType() != null ? dto.getDateType() : "INTERVAL")
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .build();

        SalePromotion saved = promoRepository.save(entity);
        return toDto(saved);
    }

    @Override
    @Transactional
    public SalePromotionDto update(Long id, SalePromotionDto dto) {
        SalePromotion entity = promoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));

        entity.setDescription(dto.getDescription());
        entity.setSecondLanguage(dto.getSecondLanguage());
        if (dto.getActive() != null) entity.setActive(dto.getActive());
        entity.setPriceBook(dto.getPriceBook());
        if (dto.getDiscountType() != null) entity.setDiscountType(dto.getDiscountType());
        if (dto.getMinRequirementType() != null) entity.setMinRequirementType(dto.getMinRequirementType());
        if (dto.getMinRequirementValue() != null) entity.setMinRequirementValue(dto.getMinRequirementValue());
        if (dto.getDiscountValueScope() != null) entity.setDiscountValueScope(dto.getDiscountValueScope());
        entity.setTargetScopeId(dto.getTargetScopeId());
        entity.setTargetScopeName(dto.getTargetScopeName());
        if (dto.getDiscountValue() != null) entity.setDiscountValue(dto.getDiscountValue());
        if (dto.getDateType() != null) entity.setDateType(dto.getDateType());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());

        SalePromotion saved = promoRepository.save(entity);
        return toDto(saved);
    }

    @Override
    @Transactional
    public SalePromotionDto toggleActive(Long id) {
        SalePromotion entity = promoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));
        entity.setActive(!Boolean.TRUE.equals(entity.getActive()));
        SalePromotion saved = promoRepository.save(entity);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        promoRepository.deleteById(id);
    }

    @Override
    public String getNextPromoCode() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        long count = promoRepository.count() + 1;
        String seq = String.format("%04d", count);
        String code = "PR-" + datePart + "-" + seq;
        while (promoRepository.existsByCode(code)) {
            count++;
            seq = String.format("%04d", count);
            code = "PR-" + datePart + "-" + seq;
        }
        return code;
    }

    private SalePromotionDto toDto(SalePromotion p) {
        return SalePromotionDto.builder()
                .id(p.getId())
                .code(p.getCode())
                .description(p.getDescription())
                .secondLanguage(p.getSecondLanguage())
                .active(p.getActive())
                .priceBook(p.getPriceBook())
                .discountType(p.getDiscountType())
                .minRequirementType(p.getMinRequirementType())
                .minRequirementValue(p.getMinRequirementValue())
                .discountValueScope(p.getDiscountValueScope())
                .targetScopeId(p.getTargetScopeId())
                .targetScopeName(p.getTargetScopeName())
                .discountValue(p.getDiscountValue())
                .dateType(p.getDateType())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}