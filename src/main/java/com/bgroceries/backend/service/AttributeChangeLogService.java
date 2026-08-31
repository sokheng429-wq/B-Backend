package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.AttributeChangeLogDto;
import com.bgroceries.backend.entity.Stocks.AttributeChangeLog;
import com.bgroceries.backend.entity.Stocks.Product;
import com.bgroceries.backend.exception.NotFoundException;
import com.bgroceries.backend.repository.AttributeChangeLogRepository;
import com.bgroceries.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD service for {@code AttributeChangeLog} — records and exposes the audit
 * trail of bulk attribute assignments made via the admin Change Attribute tool.
 */
@Service
@RequiredArgsConstructor
public class AttributeChangeLogService {

    private final AttributeChangeLogRepository attributeChangeLogRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<AttributeChangeLogDto> getAll() {
        return attributeChangeLogRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<AttributeChangeLogDto> getByProductId(Long productId) {
        return attributeChangeLogRepository.findByProductIdOrderByChangedAtDesc(productId)
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public AttributeChangeLogDto create(AttributeChangeLogDto dto) {
        Product product = findProduct(dto.getProductId());

        AttributeChangeLog log = AttributeChangeLog.builder()
                .product(product)
                .attributeName(dto.getAttributeName())
                .oldValue(dto.getOldValue())
                .newValue(dto.getNewValue())
                .reason(dto.getReason())
                .changedBy(dto.getChangedBy())
                .productName(product.getName())
                .build();

        return toDto(attributeChangeLogRepository.save(log));
    }

    @Transactional
    public AttributeChangeLogDto update(Long id, AttributeChangeLogDto dto) {
        AttributeChangeLog log = attributeChangeLogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Attribute change log not found: " + id));

        if (dto.getProductId() != null) {
            Product product = findProduct(dto.getProductId());
            log.setProduct(product);
            log.setProductName(product.getName());
        }
        if (dto.getAttributeName() != null) log.setAttributeName(dto.getAttributeName());
        if (dto.getOldValue() != null) log.setOldValue(dto.getOldValue());
        if (dto.getNewValue() != null) log.setNewValue(dto.getNewValue());
        if (dto.getReason() != null) log.setReason(dto.getReason());
        if (dto.getChangedBy() != null) log.setChangedBy(dto.getChangedBy());

        return toDto(attributeChangeLogRepository.save(log));
    }

    @Transactional
    public void delete(Long id) {
        if (!attributeChangeLogRepository.existsById(id)) {
            throw new NotFoundException("Attribute change log not found: " + id);
        }
        attributeChangeLogRepository.deleteById(id);
    }

    // ---- helpers ------------------------------------------------------------

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
    }

    private AttributeChangeLogDto toDto(AttributeChangeLog log) {
        return AttributeChangeLogDto.builder()
                .id(log.getId())
                .productId(log.getProduct().getId())
                .attributeName(log.getAttributeName())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .reason(log.getReason())
                .changedBy(log.getChangedBy())
                .changedAt(log.getChangedAt())
                .productName(log.getProductName())
                .build();
    }
}
