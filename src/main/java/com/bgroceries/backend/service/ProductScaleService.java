package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.ProductScaleDto;
import com.bgroceries.backend.entity.Product;
import com.bgroceries.backend.entity.ProductScale;
import com.bgroceries.backend.exception.NotFoundException;
import com.bgroceries.backend.repository.ProductRepository;
import com.bgroceries.backend.repository.ProductScaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD service for {@code ProductScale} — manages PLU / weigh-scale
 * configuration for products used by the admin Scales tool.
 */
@Service
@RequiredArgsConstructor
public class ProductScaleService {

    private final ProductScaleRepository productScaleRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<ProductScaleDto> getAll() {
        return productScaleRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductScaleDto> getByProductId(Long productId) {
        return productScaleRepository.findByProductId(productId).stream().map(this::toDto).toList();
    }

    @Transactional
    public ProductScaleDto create(ProductScaleDto dto) {
        Product product = findProduct(dto.getProductId());

        if (dto.getPluCode() != null && !dto.getPluCode().isBlank()
                && productScaleRepository.findByPluCode(dto.getPluCode()).isPresent()) {
            throw new IllegalArgumentException("PLU code already in use: " + dto.getPluCode());
        }

        ProductScale scale = ProductScale.builder()
                .product(product)
                .pluCode(dto.getPluCode())
                .scaleBarcode(dto.getScaleBarcode())
                .uom(dto.getUom())
                .tareWeight(dto.getTareWeight())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();

        return toDto(productScaleRepository.save(scale));
    }

    @Transactional
    public ProductScaleDto update(Long id, ProductScaleDto dto) {
        ProductScale scale = productScaleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product scale not found: " + id));

        if (dto.getProductId() != null) {
            scale.setProduct(findProduct(dto.getProductId()));
        }
        if (dto.getPluCode() != null) {
            if (!dto.getPluCode().isBlank()
                    && productScaleRepository.existsByPluCodeAndIdNot(dto.getPluCode(), id)) {
                throw new IllegalArgumentException("PLU code already in use: " + dto.getPluCode());
            }
            scale.setPluCode(dto.getPluCode());
        }
        if (dto.getScaleBarcode() != null) scale.setScaleBarcode(dto.getScaleBarcode());
        if (dto.getUom() != null) scale.setUom(dto.getUom());
        if (dto.getTareWeight() != null) scale.setTareWeight(dto.getTareWeight());
        if (dto.getActive() != null) scale.setActive(dto.getActive());

        return toDto(productScaleRepository.save(scale));
    }

    @Transactional
    public void delete(Long id) {
        if (!productScaleRepository.existsById(id)) {
            throw new NotFoundException("Product scale not found: " + id);
        }
        productScaleRepository.deleteById(id);
    }

    // ---- helpers ------------------------------------------------------------

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
    }

    private ProductScaleDto toDto(ProductScale scale) {
        return ProductScaleDto.builder()
                .id(scale.getId())
                .productId(scale.getProduct().getId())
                .productName(scale.getProduct().getName())
                .pluCode(scale.getPluCode())
                .scaleBarcode(scale.getScaleBarcode())
                .uom(scale.getUom())
                .tareWeight(scale.getTareWeight())
                .active(scale.getActive())
                .createdAt(scale.getCreatedAt())
                .updatedAt(scale.getUpdatedAt())
                .build();
    }
}
