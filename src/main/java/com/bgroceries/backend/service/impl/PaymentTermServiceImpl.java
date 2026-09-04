package com.bgroceries.backend.service.impl;

import com.bgroceries.backend.dto.PaymentTermDto;
import com.bgroceries.backend.entity.Sale.PaymentTerm;
import com.bgroceries.backend.repository.PaymentTermRepository;
import com.bgroceries.backend.service.PaymentTermService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentTermServiceImpl implements PaymentTermService {

    private final PaymentTermRepository paymentTermRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PaymentTermDto> getAllPaymentTerms(String search, String searchBy, String status) {
        List<PaymentTerm> list = paymentTermRepository.findAll();

        // Filter by Status: Active, All, Inactive
        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) {
            boolean requireActive = status.equalsIgnoreCase("ACTIVE") || status.equalsIgnoreCase("TRUE");
            list = list.stream()
                    .filter(t -> t.getActive() != null && t.getActive() == requireActive)
                    .collect(Collectors.toList());
        }

        // Filter by Search & Search By
        if (search != null && !search.isBlank()) {
            String q = search.trim().toLowerCase();
            String field = (searchBy != null && !searchBy.isBlank()) ? searchBy.trim().toLowerCase() : "any";

            list = list.stream().filter(t -> {
                switch (field) {
                    case "code":
                        return t.getCode() != null && t.getCode().toLowerCase().contains(q);
                    case "description":
                        return (t.getDescription() != null && t.getDescription().toLowerCase().contains(q)) ||
                               (t.getSecondLanguage() != null && t.getSecondLanguage().toLowerCase().contains(q));
                    case "day":
                    case "days":
                        return t.getDays() != null && String.valueOf(t.getDays()).contains(q);
                    case "any":
                    default:
                        return (t.getCode() != null && t.getCode().toLowerCase().contains(q)) ||
                               (t.getDescription() != null && t.getDescription().toLowerCase().contains(q)) ||
                               (t.getSecondLanguage() != null && t.getSecondLanguage().toLowerCase().contains(q)) ||
                               (t.getDays() != null && String.valueOf(t.getDays()).contains(q)) ||
                               (t.getNote() != null && t.getNote().toLowerCase().contains(q));
                }
            }).collect(Collectors.toList());
        }

        // Sort by days ascending (e.g. COD 0 days, Net 7, Net 15, Net 30...)
        list.sort((a, b) -> {
            int dayA = a.getDays() != null ? a.getDays() : 0;
            int dayB = b.getDays() != null ? b.getDays() : 0;
            if (dayA != dayB) return Integer.compare(dayA, dayB);
            return Long.compare(a.getId() != null ? a.getId() : 0, b.getId() != null ? b.getId() : 0);
        });

        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentTermDto getPaymentTermById(Long id) {
        PaymentTerm entity = paymentTermRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment Term not found with id: " + id));
        return toDto(entity);
    }

    @Override
    public PaymentTermDto createPaymentTerm(PaymentTermDto dto) {
        String code = (dto.getCode() != null && !dto.getCode().isBlank() && !dto.getCode().equalsIgnoreCase("Auto Generate Code"))
                ? dto.getCode().trim()
                : generateNextCode();

        PaymentTerm entity = PaymentTerm.builder()
                .code(code)
                .description(dto.getDescription() != null ? dto.getDescription().trim() : "Net Term")
                .secondLanguage(dto.getSecondLanguage())
                .days(dto.getDays() != null ? dto.getDays() : 0)
                .active(dto.getActive() != null ? dto.getActive() : true)
                .note(dto.getNote())
                .build();

        PaymentTerm saved = paymentTermRepository.save(entity);
        return toDto(saved);
    }

    @Override
    public PaymentTermDto updatePaymentTerm(Long id, PaymentTermDto dto) {
        PaymentTerm entity = paymentTermRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment Term not found with id: " + id));

        if (dto.getCode() != null && !dto.getCode().isBlank()) entity.setCode(dto.getCode().trim());
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription().trim());
        if (dto.getSecondLanguage() != null) entity.setSecondLanguage(dto.getSecondLanguage());
        if (dto.getDays() != null) entity.setDays(dto.getDays());
        if (dto.getActive() != null) entity.setActive(dto.getActive());
        if (dto.getNote() != null) entity.setNote(dto.getNote());

        PaymentTerm saved = paymentTermRepository.save(entity);
        return toDto(saved);
    }

    @Override
    public PaymentTermDto updateStatus(Long id, Boolean active) {
        PaymentTerm entity = paymentTermRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment Term not found with id: " + id));
        entity.setActive(active != null ? active : !Boolean.TRUE.equals(entity.getActive()));
        return toDto(paymentTermRepository.save(entity));
    }

    @Override
    public void deletePaymentTerm(Long id) {
        paymentTermRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public String generateNextCode() {
        long count = paymentTermRepository.count() + 1;
        return String.format("PT-%04d", count);
    }

    private PaymentTermDto toDto(PaymentTerm entity) {
        return PaymentTermDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .description(entity.getDescription())
                .secondLanguage(entity.getSecondLanguage())
                .days(entity.getDays())
                .active(entity.getActive())
                .note(entity.getNote())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
