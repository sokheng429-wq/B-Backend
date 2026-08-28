package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.CostChangeLogDto;
import com.bgroceries.backend.entity.CostChangeLog;
import com.bgroceries.backend.entity.Product;
import com.bgroceries.backend.exception.NotFoundException;
import com.bgroceries.backend.repository.CostChangeLogRepository;
import com.bgroceries.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD service for {@code CostChangeLog} — records and exposes the audit trail
 * of all cost adjustments made via the admin Cost Change tool.
 */
@Service
@RequiredArgsConstructor
public class CostChangeLogService {

    private final CostChangeLogRepository costChangeLogRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<CostChangeLogDto> getAll() {
        return costChangeLogRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<CostChangeLogDto> getByProductId(Long productId) {
        return costChangeLogRepository.findByProductIdOrderByChangedAtDesc(productId)
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public CostChangeLogDto create(CostChangeLogDto dto) {
        Product product = findProduct(dto.getProductId());

        CostChangeLog log = CostChangeLog.builder()
                .product(product)
                .oldCost(dto.getOldCost())
                .newCost(dto.getNewCost())
                .adjustmentType(dto.getAdjustmentType())
                .adjustmentValue(dto.getAdjustmentValue())
                .reason(dto.getReason())
                .changedBy(dto.getChangedBy())
                .productName(product.getName())
                .build();

        return toDto(costChangeLogRepository.save(log));
    }

    @Transactional
    public CostChangeLogDto update(Long id, CostChangeLogDto dto) {
        CostChangeLog log = costChangeLogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cost change log not found: " + id));

        if (dto.getProductId() != null) {
            Product product = findProduct(dto.getProductId());
            log.setProduct(product);
            log.setProductName(product.getName());
        }
        if (dto.getOldCost() != null) log.setOldCost(dto.getOldCost());
        if (dto.getNewCost() != null) log.setNewCost(dto.getNewCost());
        if (dto.getAdjustmentType() != null) log.setAdjustmentType(dto.getAdjustmentType());
        if (dto.getAdjustmentValue() != null) log.setAdjustmentValue(dto.getAdjustmentValue());
        if (dto.getReason() != null) log.setReason(dto.getReason());
        if (dto.getChangedBy() != null) log.setChangedBy(dto.getChangedBy());

        return toDto(costChangeLogRepository.save(log));
    }

    @Transactional
    public void delete(Long id) {
        if (!costChangeLogRepository.existsById(id)) {
            throw new NotFoundException("Cost change log not found: " + id);
        }
        costChangeLogRepository.deleteById(id);
    }

    // ---- helpers ------------------------------------------------------------

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
    }

    private CostChangeLogDto toDto(CostChangeLog log) {
        return CostChangeLogDto.builder()
                .id(log.getId())
                .productId(log.getProduct().getId())
                .oldCost(log.getOldCost())
                .newCost(log.getNewCost())
                .adjustmentType(log.getAdjustmentType())
                .adjustmentValue(log.getAdjustmentValue())
                .reason(log.getReason())
                .changedBy(log.getChangedBy())
                .changedAt(log.getChangedAt())
                .productName(log.getProductName())
                .build();
    }
}
