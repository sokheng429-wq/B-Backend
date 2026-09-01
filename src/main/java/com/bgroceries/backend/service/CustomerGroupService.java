package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.CustomerGroupDto;
import com.bgroceries.backend.entity.Sale.CustomerGroup;
import com.bgroceries.backend.repository.CustomerGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerGroupService {

    private final CustomerGroupRepository customerGroupRepository;

    @Transactional(readOnly = true)
    public List<CustomerGroupDto> getAllCustomerGroups(String search) {
        List<CustomerGroup> groups;
        if (search != null && !search.trim().isEmpty()) {
            groups = customerGroupRepository.search(search.trim());
        } else {
            groups = customerGroupRepository.findAllByOrderByCreatedAtDesc();
        }
        return groups.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CustomerGroupDto getCustomerGroupById(Long id) {
        CustomerGroup group = customerGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer group not found with id: " + id));
        return toDto(group);
    }

    @Transactional
    public CustomerGroupDto createCustomerGroup(CustomerGroupDto dto) {
        String code = dto.getCode();
        if (code == null || code.trim().isEmpty()) {
            code = generateNextCode();
        }

        CustomerGroup group = CustomerGroup.builder()
                .code(code)
                .description(dto.getDescription())
                .secondLanguage(dto.getSecondLanguage())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();

        CustomerGroup saved = customerGroupRepository.save(group);
        return toDto(saved);
    }

    @Transactional
    public CustomerGroupDto updateCustomerGroup(Long id, CustomerGroupDto dto) {
        CustomerGroup group = customerGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer group not found with id: " + id));

        if (dto.getDescription() != null) group.setDescription(dto.getDescription());
        if (dto.getSecondLanguage() != null) group.setSecondLanguage(dto.getSecondLanguage());
        if (dto.getActive() != null) group.setActive(dto.getActive());

        CustomerGroup updated = customerGroupRepository.save(group);
        return toDto(updated);
    }

    @Transactional
    public void deleteCustomerGroup(Long id) {
        if (!customerGroupRepository.existsById(id)) {
            throw new RuntimeException("Customer group not found with id: " + id);
        }
        customerGroupRepository.deleteById(id);
    }

    private String generateNextCode() {
        long count = customerGroupRepository.count() + 1;
        String candidate = String.format("CG-%04d", count);
        while (customerGroupRepository.existsByCode(candidate)) {
            count++;
            candidate = String.format("CG-%04d", count);
        }
        return candidate;
    }

    private CustomerGroupDto toDto(CustomerGroup group) {
        return CustomerGroupDto.builder()
                .id(group.getId())
                .code(group.getCode())
                .description(group.getDescription())
                .secondLanguage(group.getSecondLanguage())
                .active(group.getActive())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .build();
    }
}