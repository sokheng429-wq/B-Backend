package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.CategoryDto;
import com.bgroceries.backend.entity.Stocks.Category;
import com.bgroceries.backend.exception.ConflictException;
import com.bgroceries.backend.exception.NotFoundException;
import com.bgroceries.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD for the Category resource (admin Stocks → Categories). A blank
 * {@code code} is auto-generated as CT-0001, CT-0002… (next free sequence);
 * a provided code must stay unique — duplicates raise ConflictException
 * (409). Blank/absent strings are stored as null so the catalog stays clean.
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long id) {
        return toDto(findCategory(id));
    }

    @Transactional
    public CategoryDto createCategory(CategoryDto dto) {
        String code = normalize(dto.getCode());
        if (code == null) {
            code = generateNextCode();
        } else if (categoryRepository.findByCode(code).isPresent()) {
            throw new ConflictException("Category code already exists: " + code);
        }
        if (categoryRepository.existsByDescriptionIgnoreCase(dto.getDescription().trim())) {
            throw new ConflictException("Category already exists: " + dto.getDescription().trim());
        }
        Category category = Category.builder()
                .code(code)
                .description(dto.getDescription().trim())
                .nameKh(normalize(dto.getNameKh()))
                .active(dto.getActive() == null || dto.getActive())
                .build();
        return toDto(categoryRepository.save(category));
    }

    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto dto) {
        Category category = findCategory(id);
        String code = normalize(dto.getCode());
        if (code != null && categoryRepository.existsByCodeAndIdNot(code, id)) {
            throw new ConflictException("Category code already exists: " + code);
        }
        if (categoryRepository.existsByDescriptionIgnoreCaseAndIdNot(dto.getDescription().trim(), id)) {
            throw new ConflictException("Category already exists: " + dto.getDescription().trim());
        }
        // Keep the existing generated code when the client sends it back blank.
        if (code != null) category.setCode(code);
        category.setDescription(dto.getDescription().trim());
        category.setNameKh(normalize(dto.getNameKh()));
        if (dto.getActive() != null) category.setActive(dto.getActive());
        return toDto(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        categoryRepository.delete(findCategory(id));
    }

    /** CT-#### — one past the highest existing sequence, zero-padded to 4 digits. */
    private String generateNextCode() {
        long next = categoryRepository.maxSequenceNumber() + 1;
        return String.format("CT-%04d", next);
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
    }

    /** Trim, and turn blanks into null so optional columns stay clean. */
    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private CategoryDto toDto(Category c) {
        return CategoryDto.builder()
                .id(c.getId())
                .code(c.getCode())
                .description(c.getDescription())
                .nameKh(c.getNameKh())
                .active(c.getActive())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
