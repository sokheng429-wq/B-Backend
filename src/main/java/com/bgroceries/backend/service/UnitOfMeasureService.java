package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.UnitOfMeasureDto;
import com.bgroceries.backend.entity.UnitOfMeasure;
import com.bgroceries.backend.exception.ConflictException;
import com.bgroceries.backend.exception.NotFoundException;
import com.bgroceries.backend.repository.UnitOfMeasureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD for the UnitOfMeasure resource (admin Stocks → Unit of Measure). A
 * blank {@code code} is auto-generated as UN-0001, UN-0002… (next free
 * sequence); a provided code must stay unique — duplicates raise
 * ConflictException (409). {@code factor} carries the optional conversion
 * factor relative to the base unit (e.g. kilogram = 1000). Blank/absent
 * strings are stored as null so the catalog stays clean.
 */
@Service
@RequiredArgsConstructor
public class UnitOfMeasureService {

    private final UnitOfMeasureRepository unitOfMeasureRepository;

    @Transactional(readOnly = true)
    public List<UnitOfMeasureDto> getAllUnits() {
        return unitOfMeasureRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public UnitOfMeasureDto getUnitById(Long id) {
        return toDto(findUnit(id));
    }

    @Transactional
    public UnitOfMeasureDto createUnit(UnitOfMeasureDto dto) {
        String code = normalize(dto.getCode());
        if (code == null) {
            code = generateNextCode();
        } else if (unitOfMeasureRepository.findByCode(code).isPresent()) {
            throw new ConflictException("Unit code already exists: " + code);
        }
        if (unitOfMeasureRepository.existsByDescriptionIgnoreCase(dto.getDescription().trim())) {
            throw new ConflictException("Unit already exists: " + dto.getDescription().trim());
        }
        UnitOfMeasure unit = UnitOfMeasure.builder()
                .code(code)
                .description(dto.getDescription().trim())
                .nameKh(normalize(dto.getNameKh()))
                .factor(dto.getFactor())
                .active(dto.getActive() == null || dto.getActive())
                .build();
        return toDto(unitOfMeasureRepository.save(unit));
    }

    @Transactional
    public UnitOfMeasureDto updateUnit(Long id, UnitOfMeasureDto dto) {
        UnitOfMeasure unit = findUnit(id);
        String code = normalize(dto.getCode());
        if (code != null && unitOfMeasureRepository.existsByCodeAndIdNot(code, id)) {
            throw new ConflictException("Unit code already exists: " + code);
        }
        if (unitOfMeasureRepository.existsByDescriptionIgnoreCaseAndIdNot(dto.getDescription().trim(), id)) {
            throw new ConflictException("Unit already exists: " + dto.getDescription().trim());
        }
        // Keep the existing generated code when the client sends it back blank.
        if (code != null) unit.setCode(code);
        unit.setDescription(dto.getDescription().trim());
        unit.setNameKh(normalize(dto.getNameKh()));
        // A null factor clears the stored value — the client owns the field.
        unit.setFactor(dto.getFactor());
        if (dto.getActive() != null) unit.setActive(dto.getActive());
        return toDto(unitOfMeasureRepository.save(unit));
    }

    @Transactional
    public void deleteUnit(Long id) {
        unitOfMeasureRepository.delete(findUnit(id));
    }

    /** UN-#### — one past the highest existing sequence, zero-padded to 4 digits. */
    private String generateNextCode() {
        long next = unitOfMeasureRepository.maxSequenceNumber() + 1;
        return String.format("UN-%04d", next);
    }

    private UnitOfMeasure findUnit(Long id) {
        return unitOfMeasureRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Unit not found"));
    }

    /** Trim, and turn blanks into null so optional columns stay clean. */
    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private UnitOfMeasureDto toDto(UnitOfMeasure u) {
        return UnitOfMeasureDto.builder()
                .id(u.getId())
                .code(u.getCode())
                .description(u.getDescription())
                .nameKh(u.getNameKh())
                .factor(u.getFactor())
                .active(u.getActive())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }
}
