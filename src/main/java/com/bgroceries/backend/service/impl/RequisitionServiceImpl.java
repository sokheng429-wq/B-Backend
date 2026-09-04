package com.bgroceries.backend.service.impl;

import com.bgroceries.backend.dto.RequisitionDto;
import com.bgroceries.backend.dto.RequisitionItemDto;
import com.bgroceries.backend.entity.Purchase.Requisition;
import com.bgroceries.backend.entity.Purchase.RequisitionItem;
import com.bgroceries.backend.repository.RequisitionRepository;
import com.bgroceries.backend.service.RequisitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RequisitionServiceImpl implements RequisitionService {

    private final RequisitionRepository requisitionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RequisitionDto> getAllRequisitions(
            String search,
            String searchBy,
            LocalDate fromDate,
            LocalDate toDate,
            String status
    ) {
        List<Requisition> list = requisitionRepository.findAllByOrderByCreatedAtDesc();

        // 1. Filter by Status: OPEN, PARTIAL, COMPLETED, VOIDED, ALL
        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) {
            String targetStatus = status.trim().toUpperCase();
            list = list.stream()
                    .filter(r -> r.getStatus() != null && r.getStatus().equalsIgnoreCase(targetStatus))
                    .collect(Collectors.toList());
        }

        // 2. Filter by Date range
        if (fromDate != null) {
            list = list.stream()
                    .filter(r -> r.getDate() != null && !r.getDate().isBefore(fromDate))
                    .collect(Collectors.toList());
        }
        if (toDate != null) {
            list = list.stream()
                    .filter(r -> r.getDate() != null && !r.getDate().isAfter(toDate))
                    .collect(Collectors.toList());
        }

        // 3. Filter by Search & Search By
        // Options: Any, Code, Reference, Reference Code, User Name
        if (search != null && !search.isBlank()) {
            String q = search.trim().toLowerCase();
            String field = (searchBy != null && !searchBy.isBlank()) ? searchBy.trim().toLowerCase() : "any";

            list = list.stream().filter(r -> {
                switch (field) {
                    case "code":
                        return r.getCode() != null && r.getCode().toLowerCase().contains(q);
                    case "reference":
                        return r.getReference() != null && r.getReference().toLowerCase().contains(q);
                    case "reference code":
                    case "referencecode":
                    case "reference_code":
                        return r.getReferenceCode() != null && r.getReferenceCode().toLowerCase().contains(q);
                    case "user name":
                    case "username":
                    case "user_name":
                        return r.getUserName() != null && r.getUserName().toLowerCase().contains(q);
                    case "any":
                    default:
                        return (r.getCode() != null && r.getCode().toLowerCase().contains(q)) ||
                               (r.getReference() != null && r.getReference().toLowerCase().contains(q)) ||
                               (r.getReferenceCode() != null && r.getReferenceCode().toLowerCase().contains(q)) ||
                               (r.getUserName() != null && r.getUserName().toLowerCase().contains(q)) ||
                               (r.getRequisitionType() != null && r.getRequisitionType().toLowerCase().contains(q)) ||
                               (r.getTemplateName() != null && r.getTemplateName().toLowerCase().contains(q)) ||
                               (r.getNote() != null && r.getNote().toLowerCase().contains(q));
                }
            }).collect(Collectors.toList());
        }

        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RequisitionDto getRequisitionById(Long id) {
        Requisition entity = requisitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Requisition not found with id: " + id));
        return toDto(entity);
    }

    @Override
    public RequisitionDto createRequisition(RequisitionDto dto) {
        String code = (dto.getCode() != null && !dto.getCode().isBlank() && !dto.getCode().equalsIgnoreCase("Auto Generate Code"))
                ? dto.getCode().trim()
                : generateNextCode();

        Requisition entity = Requisition.builder()
                .code(code)
                .date(dto.getDate() != null ? dto.getDate() : LocalDate.now())
                .requireDate(dto.getRequireDate() != null ? dto.getRequireDate() : LocalDate.now().plusDays(7))
                .templateName(dto.getTemplateName() != null ? dto.getTemplateName() : "Standard Template")
                .requisitionType(dto.getRequisitionType() != null ? dto.getRequisitionType() : "Store Replenishment")
                .reference(dto.getReference())
                .referenceCode(dto.getReferenceCode())
                .userName(dto.getUserName() != null ? dto.getUserName() : "Badmin")
                .status((dto.getStatus() != null && !dto.getStatus().isBlank()) ? dto.getStatus().toUpperCase() : "OPEN")
                .note(dto.getNote())
                .items(new ArrayList<>())
                .build();

        double totalAmount = 0.0;
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (RequisitionItemDto itemDto : dto.getItems()) {
                double qty = itemDto.getRequisitionQty() != null ? itemDto.getRequisitionQty() : 1.0;
                double cost = itemDto.getCost() != null ? itemDto.getCost() : 0.0;
                double lineTotal = itemDto.getTotal() != null ? itemDto.getTotal() : (qty * cost);
                totalAmount += lineTotal;

                RequisitionItem item = RequisitionItem.builder()
                        .productId(itemDto.getProductId())
                        .code(itemDto.getCode())
                        .barcode(itemDto.getBarcode())
                        .description(itemDto.getDescription())
                        .requisitionQty(qty)
                        .uom(itemDto.getUom() != null ? itemDto.getUom() : "Pcs")
                        .cost(cost)
                        .total(lineTotal)
                        .build();
                entity.addItem(item);
            }
        }
        entity.setRequisitionAmount(totalAmount);

        Requisition saved = requisitionRepository.save(entity);
        return toDto(saved);
    }

    @Override
    public RequisitionDto updateRequisition(Long id, RequisitionDto dto) {
        Requisition entity = requisitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Requisition not found with id: " + id));

        if (dto.getDate() != null) entity.setDate(dto.getDate());
        if (dto.getRequireDate() != null) entity.setRequireDate(dto.getRequireDate());
        if (dto.getTemplateName() != null) entity.setTemplateName(dto.getTemplateName());
        if (dto.getRequisitionType() != null) entity.setRequisitionType(dto.getRequisitionType());
        if (dto.getReference() != null) entity.setReference(dto.getReference());
        if (dto.getReferenceCode() != null) entity.setReferenceCode(dto.getReferenceCode());
        if (dto.getUserName() != null) entity.setUserName(dto.getUserName());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus().toUpperCase());
        if (dto.getNote() != null) entity.setNote(dto.getNote());

        if (dto.getItems() != null) {
            entity.getItems().clear();
            double totalAmount = 0.0;
            for (RequisitionItemDto itemDto : dto.getItems()) {
                double qty = itemDto.getRequisitionQty() != null ? itemDto.getRequisitionQty() : 1.0;
                double cost = itemDto.getCost() != null ? itemDto.getCost() : 0.0;
                double lineTotal = itemDto.getTotal() != null ? itemDto.getTotal() : (qty * cost);
                totalAmount += lineTotal;

                RequisitionItem item = RequisitionItem.builder()
                        .productId(itemDto.getProductId())
                        .code(itemDto.getCode())
                        .barcode(itemDto.getBarcode())
                        .description(itemDto.getDescription())
                        .requisitionQty(qty)
                        .uom(itemDto.getUom() != null ? itemDto.getUom() : "Pcs")
                        .cost(cost)
                        .total(lineTotal)
                        .build();
                entity.addItem(item);
            }
            entity.setRequisitionAmount(totalAmount);
        }

        Requisition saved = requisitionRepository.save(entity);
        return toDto(saved);
    }

    @Override
    public RequisitionDto updateStatus(Long id, String status) {
        Requisition entity = requisitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Requisition not found with id: " + id));
        entity.setStatus(status.toUpperCase());
        return toDto(requisitionRepository.save(entity));
    }

    @Override
    public void deleteRequisition(Long id) {
        Requisition entity = requisitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Requisition not found with id: " + id));
        requisitionRepository.delete(entity);
    }

    @Override
    public String generateNextCode() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = requisitionRepository.count() + 1;
        String formatted = String.format("REQ-%s-%04d", datePart, count);

        while (requisitionRepository.findByCode(formatted).isPresent()) {
            count++;
            formatted = String.format("REQ-%s-%04d", datePart, count);
        }
        return formatted;
    }

    private RequisitionDto toDto(Requisition entity) {
        List<RequisitionItemDto> itemDtos = new ArrayList<>();
        if (entity.getItems() != null) {
            itemDtos = entity.getItems().stream().map(i -> RequisitionItemDto.builder()
                    .id(i.getId())
                    .productId(i.getProductId())
                    .code(i.getCode())
                    .barcode(i.getBarcode())
                    .description(i.getDescription())
                    .requisitionQty(i.getRequisitionQty())
                    .uom(i.getUom())
                    .cost(i.getCost())
                    .total(i.getTotal())
                    .build()).collect(Collectors.toList());
        }

        return RequisitionDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .date(entity.getDate())
                .requireDate(entity.getRequireDate())
                .templateName(entity.getTemplateName())
                .requisitionType(entity.getRequisitionType())
                .requisitionAmount(entity.getRequisitionAmount())
                .reference(entity.getReference())
                .referenceCode(entity.getReferenceCode())
                .userName(entity.getUserName())
                .status(entity.getStatus())
                .note(entity.getNote())
                .items(itemDtos)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
