package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.AttributeDto;
import com.bgroceries.backend.entity.Attribute;
import com.bgroceries.backend.exception.ConflictException;
import com.bgroceries.backend.exception.NotFoundException;
import com.bgroceries.backend.repository.AttributeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD for the Attribute resource (admin Stocks → Attributes) — same flow as
 * CategoryService. A blank {@code code} is auto-generated as AT-0001, AT-0002…
 * (next free sequence); a provided code must stay unique — duplicates raise
 * ConflictException (409). Blank/absent strings are stored as null so the
 * catalog stays clean.
 */
@Service
@RequiredArgsConstructor
public class AttributeService {

    private final AttributeRepository attributeRepository;

    @Transactional(readOnly = true)
    public List<AttributeDto> getAllAttributes() {
        return attributeRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public AttributeDto getAttributeById(Long id) {
        return toDto(findAttribute(id));
    }

    @Transactional
    public AttributeDto createAttribute(AttributeDto dto) {
        String code = normalize(dto.getCode());
        if (code == null) {
            code = generateNextCode();
        } else if (attributeRepository.findByCode(code).isPresent()) {
            throw new ConflictException("Attribute code already exists: " + code);
        }
        if (attributeRepository.existsByDescriptionIgnoreCase(dto.getDescription().trim())) {
            throw new ConflictException("Attribute already exists: " + dto.getDescription().trim());
        }
        Attribute attribute = Attribute.builder()
                .code(code)
                .description(dto.getDescription().trim())
                .nameKh(normalize(dto.getNameKh()))
                .type(normalize(dto.getType()))
                .values(normalize(dto.getValues()))
                .active(dto.getActive() == null || dto.getActive())
                .build();
        return toDto(attributeRepository.save(attribute));
    }

    @Transactional
    public AttributeDto updateAttribute(Long id, AttributeDto dto) {
        Attribute attribute = findAttribute(id);
        String code = normalize(dto.getCode());
        if (code != null && attributeRepository.existsByCodeAndIdNot(code, id)) {
            throw new ConflictException("Attribute code already exists: " + code);
        }
        if (attributeRepository.existsByDescriptionIgnoreCaseAndIdNot(dto.getDescription().trim(), id)) {
            throw new ConflictException("Attribute already exists: " + dto.getDescription().trim());
        }
        // Keep the existing generated code when the client sends it back blank.
        if (code != null) attribute.setCode(code);
        attribute.setDescription(dto.getDescription().trim());
        attribute.setNameKh(normalize(dto.getNameKh()));
        attribute.setType(normalize(dto.getType()));
        attribute.setValues(normalize(dto.getValues()));
        if (dto.getActive() != null) attribute.setActive(dto.getActive());
        return toDto(attributeRepository.save(attribute));
    }

    @Transactional
    public void deleteAttribute(Long id) {
        attributeRepository.delete(findAttribute(id));
    }

    /** AT-#### — one past the highest existing sequence, zero-padded to 4 digits. */
    private String generateNextCode() {
        long next = attributeRepository.maxSequenceNumber() + 1;
        return String.format("AT-%04d", next);
    }

    private Attribute findAttribute(Long id) {
        return attributeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Attribute not found"));
    }

    /** Trim, and turn blanks into null so optional columns stay clean. */
    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private AttributeDto toDto(Attribute a) {
        return AttributeDto.builder()
                .id(a.getId())
                .code(a.getCode())
                .description(a.getDescription())
                .nameKh(a.getNameKh())
                .type(a.getType())
                .values(a.getValues())
                .active(a.getActive())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
