package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.BrandDto;
import com.bgroceries.backend.entity.Stocks.Brand;
import com.bgroceries.backend.exception.ConflictException;
import com.bgroceries.backend.exception.NotFoundException;
import com.bgroceries.backend.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD for the Brand resource (admin Stocks → Brands). A blank
 * {@code code} is auto-generated as BR-0001, BR-0002… (next free sequence);
 * a provided code must stay unique — duplicates raise ConflictException
 * (409). Blank/absent strings are stored as null so the catalog stays clean.
 */
@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;

    @Transactional(readOnly = true)
    public List<BrandDto> getAllBrands() {
        return brandRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public BrandDto getBrandById(Long id) {
        return toDto(findBrand(id));
    }

    @Transactional
    public BrandDto createBrand(BrandDto dto) {
        String code = normalize(dto.getCode());
        if (code == null) {
            code = generateNextCode();
        } else if (brandRepository.findByCode(code).isPresent()) {
            throw new ConflictException("Brand code already exists: " + code);
        }
        if (brandRepository.existsByDescriptionIgnoreCase(dto.getDescription().trim())) {
            throw new ConflictException("Brand already exists: " + dto.getDescription().trim());
        }
        Brand brand = Brand.builder()
                .code(code)
                .description(dto.getDescription().trim())
                .nameKh(normalize(dto.getNameKh()))
                .active(dto.getActive() == null || dto.getActive())
                .build();
        return toDto(brandRepository.save(brand));
    }

    @Transactional
    public BrandDto updateBrand(Long id, BrandDto dto) {
        Brand brand = findBrand(id);
        String code = normalize(dto.getCode());
        if (code != null && brandRepository.existsByCodeAndIdNot(code, id)) {
            throw new ConflictException("Brand code already exists: " + code);
        }
        if (brandRepository.existsByDescriptionIgnoreCaseAndIdNot(dto.getDescription().trim(), id)) {
            throw new ConflictException("Brand already exists: " + dto.getDescription().trim());
        }
        // Keep the existing generated code when the client sends it back blank.
        if (code != null) brand.setCode(code);
        brand.setDescription(dto.getDescription().trim());
        brand.setNameKh(normalize(dto.getNameKh()));
        if (dto.getActive() != null) brand.setActive(dto.getActive());
        return toDto(brandRepository.save(brand));
    }

    @Transactional
    public void deleteBrand(Long id) {
        brandRepository.delete(findBrand(id));
    }

    /** BR-#### — one past the highest existing sequence, zero-padded to 4 digits. */
    private String generateNextCode() {
        long next = brandRepository.maxSequenceNumber() + 1;
        return String.format("BR-%04d", next);
    }

    private Brand findBrand(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Brand not found"));
    }

    /** Trim, and turn blanks into null so optional columns stay clean. */
    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private BrandDto toDto(Brand b) {
        return BrandDto.builder()
                .id(b.getId())
                .code(b.getCode())
                .description(b.getDescription())
                .nameKh(b.getNameKh())
                .active(b.getActive())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
