package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.ProductGroupDto;
import com.bgroceries.backend.entity.ProductGroup;
import com.bgroceries.backend.exception.ConflictException;
import com.bgroceries.backend.exception.NotFoundException;
import com.bgroceries.backend.repository.ProductGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD for the ProductGroup resource (admin Stocks → Groups). A blank
 * {@code code} is auto-generated as PG-0001, PG-0002… (next free sequence);
 * a provided code must stay unique — duplicates raise ConflictException
 * (409). Blank/absent strings are stored as null so the catalog stays clean.
 */
@Service
@RequiredArgsConstructor
public class ProductGroupService {

    private final ProductGroupRepository productGroupRepository;

    @Transactional(readOnly = true)
    public List<ProductGroupDto> getAllGroups() {
        return productGroupRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductGroupDto getGroupById(Long id) {
        return toDto(findGroup(id));
    }

    @Transactional
    public ProductGroupDto createGroup(ProductGroupDto dto) {
        String code = normalize(dto.getCode());
        if (code == null) {
            code = generateNextCode();
        } else if (productGroupRepository.findByCode(code).isPresent()) {
            throw new ConflictException("Product group code already exists: " + code);
        }
        if (productGroupRepository.existsByDescriptionIgnoreCase(dto.getDescription().trim())) {
            throw new ConflictException("Product group already exists: " + dto.getDescription().trim());
        }
        ProductGroup group = ProductGroup.builder()
                .code(code)
                .description(dto.getDescription().trim())
                .nameKh(normalize(dto.getNameKh()))
                .active(dto.getActive() == null || dto.getActive())
                .favorite(Boolean.TRUE.equals(dto.getFavorite()))
                .build();
        return toDto(productGroupRepository.save(group));
    }

    @Transactional
    public ProductGroupDto updateGroup(Long id, ProductGroupDto dto) {
        ProductGroup group = findGroup(id);
        String code = normalize(dto.getCode());
        if (code != null && productGroupRepository.existsByCodeAndIdNot(code, id)) {
            throw new ConflictException("Product group code already exists: " + code);
        }
        if (productGroupRepository.existsByDescriptionIgnoreCaseAndIdNot(dto.getDescription().trim(), id)) {
            throw new ConflictException("Product group already exists: " + dto.getDescription().trim());
        }
        // Keep the existing generated code when the client sends it back blank.
        if (code != null) group.setCode(code);
        group.setDescription(dto.getDescription().trim());
        group.setNameKh(normalize(dto.getNameKh()));
        if (dto.getActive() != null) group.setActive(dto.getActive());
        if (dto.getFavorite() != null) group.setFavorite(dto.getFavorite());
        return toDto(productGroupRepository.save(group));
    }

    @Transactional
    public void deleteGroup(Long id) {
        productGroupRepository.delete(findGroup(id));
    }

    /** PG-#### — one past the highest existing sequence, zero-padded to 4 digits. */
    private String generateNextCode() {
        long next = productGroupRepository.maxSequenceNumber() + 1;
        return String.format("PG-%04d", next);
    }

    private ProductGroup findGroup(Long id) {
        return productGroupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product group not found"));
    }

    /** Trim, and turn blanks into null so optional columns stay clean. */
    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ProductGroupDto toDto(ProductGroup g) {
        return ProductGroupDto.builder()
                .id(g.getId())
                .code(g.getCode())
                .description(g.getDescription())
                .nameKh(g.getNameKh())
                .active(g.getActive())
                .favorite(g.getFavorite())
                .createdAt(g.getCreatedAt())
                .updatedAt(g.getUpdatedAt())
                .build();
    }
}
