package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.PriceHistoryDto;
import com.bgroceries.backend.entity.Stocks.PriceHistory;
import com.bgroceries.backend.entity.Stocks.Product;
import com.bgroceries.backend.exception.NotFoundException;
import com.bgroceries.backend.repository.PriceHistoryRepository;
import com.bgroceries.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD service for {@code PriceHistory} — records and exposes the audit trail
 * of all selling price changes made via the admin Products Prices tool.
 */
@Service
@RequiredArgsConstructor
public class PriceHistoryService {

    private final PriceHistoryRepository priceHistoryRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<PriceHistoryDto> getAll() {
        return priceHistoryRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<PriceHistoryDto> getByProductId(Long productId) {
        return priceHistoryRepository.findByProductIdOrderByChangedAtDesc(productId)
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public PriceHistoryDto create(PriceHistoryDto dto) {
        Product product = findProduct(dto.getProductId());

        PriceHistory history = PriceHistory.builder()
                .product(product)
                .oldPrice(dto.getOldPrice())
                .newPrice(dto.getNewPrice())
                .changeType(dto.getChangeType())
                .markupPercent(dto.getMarkupPercent())
                .reason(dto.getReason())
                .changedBy(dto.getChangedBy())
                .productName(product.getName())
                .build();

        return toDto(priceHistoryRepository.save(history));
    }

    @Transactional
    public PriceHistoryDto update(Long id, PriceHistoryDto dto) {
        PriceHistory history = priceHistoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Price history not found: " + id));

        if (dto.getProductId() != null) {
            Product product = findProduct(dto.getProductId());
            history.setProduct(product);
            history.setProductName(product.getName());
        }
        if (dto.getOldPrice() != null) history.setOldPrice(dto.getOldPrice());
        if (dto.getNewPrice() != null) history.setNewPrice(dto.getNewPrice());
        if (dto.getChangeType() != null) history.setChangeType(dto.getChangeType());
        if (dto.getMarkupPercent() != null) history.setMarkupPercent(dto.getMarkupPercent());
        if (dto.getReason() != null) history.setReason(dto.getReason());
        if (dto.getChangedBy() != null) history.setChangedBy(dto.getChangedBy());

        return toDto(priceHistoryRepository.save(history));
    }

    @Transactional
    public void delete(Long id) {
        if (!priceHistoryRepository.existsById(id)) {
            throw new NotFoundException("Price history not found: " + id);
        }
        priceHistoryRepository.deleteById(id);
    }

    // ---- helpers ------------------------------------------------------------

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
    }

    private PriceHistoryDto toDto(PriceHistory history) {
        return PriceHistoryDto.builder()
                .id(history.getId())
                .productId(history.getProduct().getId())
                .oldPrice(history.getOldPrice())
                .newPrice(history.getNewPrice())
                .changeType(history.getChangeType())
                .markupPercent(history.getMarkupPercent())
                .reason(history.getReason())
                .changedBy(history.getChangedBy())
                .changedAt(history.getChangedAt())
                .productName(history.getProductName())
                .build();
    }
}
