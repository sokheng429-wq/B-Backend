package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.SupplierGroupDto;
import com.bgroceries.backend.entity.SupplierGroup;
import com.bgroceries.backend.exception.ConflictException;
import com.bgroceries.backend.exception.NotFoundException;
import com.bgroceries.backend.repository.SupplierGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD for the SupplierGroup resource (admin Stocks → Suppliers Group). A
 * blank {@code code} is auto-generated as SG-0001, SG-0002… (next free
 * sequence); a provided code must stay unique — duplicates raise
 * ConflictException (409). Blank/absent strings are stored as null so the
 * catalog stays clean.
 */
@Service
@RequiredArgsConstructor
public class SupplierGroupService {

    private final SupplierGroupRepository supplierGroupRepository;

    @Transactional(readOnly = true)
    public List<SupplierGroupDto> getAllGroups() {
        return supplierGroupRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public SupplierGroupDto getGroupById(Long id) {
        return toDto(findGroup(id));
    }

    @Transactional
    public SupplierGroupDto createGroup(SupplierGroupDto dto) {
        String code = normalize(dto.getCode());
        if (code == null) {
            code = generateNextCode();
        } else if (supplierGroupRepository.findByCode(code).isPresent()) {
            throw new ConflictException("Supplier group code already exists: " + code);
        }
        if (supplierGroupRepository.existsByDescriptionIgnoreCase(dto.getDescription().trim())) {
            throw new ConflictException("Supplier group already exists: " + dto.getDescription().trim());
        }
        SupplierGroup group = SupplierGroup.builder()
                .code(code)
                .description(dto.getDescription().trim())
                .nameKh(normalize(dto.getNameKh()))
                .active(dto.getActive() == null || dto.getActive())
                .build();
        return toDto(supplierGroupRepository.save(group));
    }

    @Transactional
    public SupplierGroupDto updateGroup(Long id, SupplierGroupDto dto) {
        SupplierGroup group = findGroup(id);
        String code = normalize(dto.getCode());
        if (code != null && supplierGroupRepository.existsByCodeAndIdNot(code, id)) {
            throw new ConflictException("Supplier group code already exists: " + code);
        }
        if (supplierGroupRepository.existsByDescriptionIgnoreCaseAndIdNot(dto.getDescription().trim(), id)) {
            throw new ConflictException("Supplier group already exists: " + dto.getDescription().trim());
        }
        // Keep the existing generated code when the client sends it back blank.
        if (code != null) group.setCode(code);
        group.setDescription(dto.getDescription().trim());
        group.setNameKh(normalize(dto.getNameKh()));
        if (dto.getActive() != null) group.setActive(dto.getActive());
        return toDto(supplierGroupRepository.save(group));
    }

    @Transactional
    public void deleteGroup(Long id) {
        supplierGroupRepository.delete(findGroup(id));
    }

    /** SG-#### — one past the highest existing sequence, zero-padded to 4 digits. */
    private String generateNextCode() {
        long next = supplierGroupRepository.maxSequenceNumber() + 1;
        return String.format("SG-%04d", next);
    }

    private SupplierGroup findGroup(Long id) {
        return supplierGroupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Supplier group not found"));
    }

    /** Trim, and turn blanks into null so optional columns stay clean. */
    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private SupplierGroupDto toDto(SupplierGroup g) {
        return SupplierGroupDto.builder()
                .id(g.getId())
                .code(g.getCode())
                .description(g.getDescription())
                .nameKh(g.getNameKh())
                .active(g.getActive())
                .createdAt(g.getCreatedAt())
                .updatedAt(g.getUpdatedAt())
                .build();
    }
}
