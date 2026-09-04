package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.ReturnReceiptPODto;
import com.bgroceries.backend.dto.ReturnReceiptPOItemDto;
import com.bgroceries.backend.entity.Purchase.ReturnReceiptPO;
import com.bgroceries.backend.entity.Purchase.ReturnReceiptPOItem;
import com.bgroceries.backend.repository.ReturnReceiptPORepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReturnReceiptPOServiceImpl implements ReturnReceiptPOService {

    private final ReturnReceiptPORepository returnReceiptPORepository;

    @Override
    @Transactional(readOnly = true)
    public List<ReturnReceiptPODto> getAllReturnReceiptPOs(String search, String searchBy, LocalDate fromDate, LocalDate toDate, String outlet, String status) {
        Specification<ReturnReceiptPO> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.trim().isEmpty()) {
                String q = "%" + search.trim().toLowerCase() + "%";
                String mode = searchBy != null ? searchBy.trim().toLowerCase() : "any";

                if (mode.contains("return receipt") || mode.contains("return po") || mode.contains("return")) {
                    predicates.add(cb.like(cb.lower(root.get("returnPoCode")), q));
                } else if (mode.contains("po")) {
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("poCode")), q),
                            cb.like(cb.lower(root.get("receiptPoCode")), q)
                    ));
                } else if (mode.contains("supplier")) {
                    predicates.add(cb.like(cb.lower(root.get("supplier")), q));
                } else {
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("returnPoCode")), q),
                            cb.like(cb.lower(root.get("poCode")), q),
                            cb.like(cb.lower(root.get("receiptPoCode")), q),
                            cb.like(cb.lower(root.get("supplier")), q),
                            cb.like(cb.lower(root.get("reference")), q),
                            cb.like(cb.lower(root.get("reason")), q),
                            cb.like(cb.lower(root.get("username")), q)
                    ));
                }
            }

            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), toDate));
            }
            if (outlet != null && !outlet.trim().isEmpty() && !outlet.equalsIgnoreCase("all") && !outlet.equalsIgnoreCase("any")) {
                predicates.add(cb.equal(cb.lower(root.get("outlet")), outlet.trim().toLowerCase()));
            }
            if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("all") && !status.equalsIgnoreCase("any")) {
                predicates.add(cb.equal(cb.upper(root.get("status")), status.trim().toUpperCase()));
            }

            query.orderBy(cb.desc(root.get("date")), cb.desc(root.get("id")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return returnReceiptPORepository.findAll(spec).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnReceiptPODto getReturnReceiptPOById(Long id) {
        ReturnReceiptPO r = returnReceiptPORepository.findByIdWithItems(id)
                .orElseThrow(() -> new RuntimeException("ReturnReceiptPO not found with id: " + id));
        return mapToDto(r);
    }

    @Override
    @Transactional
    public ReturnReceiptPODto createReturnReceiptPO(ReturnReceiptPODto dto) {
        String code = dto.getReturnPoCode();
        if (code == null || code.isBlank()) {
            code = generateNextCode();
        }

        ReturnReceiptPO ret = ReturnReceiptPO.builder()
                .returnPoCode(code)
                .poCode(dto.getPoCode())
                .receiptPoCode(dto.getReceiptPoCode())
                .date(dto.getDate() != null ? dto.getDate() : LocalDate.now())
                .supplier(dto.getSupplier())
                .supplierId(dto.getSupplierId())
                .amount(dto.getAmount() != null ? dto.getAmount() : 0.0)
                .status(dto.getStatus() != null && !dto.getStatus().isBlank() ? dto.getStatus().toUpperCase() : "OPEN")
                .outlet(dto.getOutlet())
                .username(dto.getUsername() != null && !dto.getUsername().isBlank() ? dto.getUsername() : "Badmin")
                .reason(dto.getReason())
                .reference(dto.getReference())
                .note(dto.getNote())
                .build();

        double totalAmount = 0.0;
        if (dto.getItems() != null) {
            for (ReturnReceiptPOItemDto itemDto : dto.getItems()) {
                double q = itemDto.getQty() != null ? itemDto.getQty() : 1.0;
                double c = itemDto.getCost() != null ? itemDto.getCost() : 0.0;
                double lineTotal = q * c;

                ReturnReceiptPOItem item = ReturnReceiptPOItem.builder()
                        .productId(itemDto.getProductId())
                        .code(itemDto.getCode())
                        .barcode(itemDto.getBarcode())
                        .description(itemDto.getDescription())
                        .qty(q)
                        .uom(itemDto.getUom() != null ? itemDto.getUom() : "Pcs")
                        .cost(c)
                        .total(lineTotal)
                        .build();

                ret.addItem(item);
                totalAmount += lineTotal;
            }
        }

        ret.setAmount(dto.getAmount() != null && dto.getAmount() > 0 ? dto.getAmount() : totalAmount);

        ReturnReceiptPO saved = returnReceiptPORepository.save(ret);
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public ReturnReceiptPODto updateReturnReceiptPO(Long id, ReturnReceiptPODto dto) {
        ReturnReceiptPO ret = returnReceiptPORepository.findByIdWithItems(id)
                .orElseThrow(() -> new RuntimeException("ReturnReceiptPO not found with id: " + id));

        ret.setPoCode(dto.getPoCode());
        ret.setReceiptPoCode(dto.getReceiptPoCode());
        ret.setDate(dto.getDate());
        ret.setSupplier(dto.getSupplier());
        ret.setSupplierId(dto.getSupplierId());
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            ret.setStatus(dto.getStatus().toUpperCase());
        }
        ret.setOutlet(dto.getOutlet());
        ret.setReason(dto.getReason());
        ret.setReference(dto.getReference());
        ret.setNote(dto.getNote());

        ret.getItems().clear();
        double totalAmount = 0.0;

        if (dto.getItems() != null) {
            for (ReturnReceiptPOItemDto itemDto : dto.getItems()) {
                double q = itemDto.getQty() != null ? itemDto.getQty() : 1.0;
                double c = itemDto.getCost() != null ? itemDto.getCost() : 0.0;
                double lineTotal = q * c;

                ReturnReceiptPOItem item = ReturnReceiptPOItem.builder()
                        .productId(itemDto.getProductId())
                        .code(itemDto.getCode())
                        .barcode(itemDto.getBarcode())
                        .description(itemDto.getDescription())
                        .qty(q)
                        .uom(itemDto.getUom() != null ? itemDto.getUom() : "Pcs")
                        .cost(c)
                        .total(lineTotal)
                        .build();

                ret.addItem(item);
                totalAmount += lineTotal;
            }
        }

        ret.setAmount(dto.getAmount() != null && dto.getAmount() > 0 ? dto.getAmount() : totalAmount);

        ReturnReceiptPO updated = returnReceiptPORepository.save(ret);
        return mapToDto(updated);
    }

    @Override
    @Transactional
    public ReturnReceiptPODto updateStatus(Long id, String status) {
        ReturnReceiptPO ret = returnReceiptPORepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ReturnReceiptPO not found with id: " + id));
        ret.setStatus(status.toUpperCase());
        return mapToDto(returnReceiptPORepository.save(ret));
    }

    @Override
    @Transactional
    public void deleteReturnReceiptPO(Long id) {
        ReturnReceiptPO ret = returnReceiptPORepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ReturnReceiptPO not found with id: " + id));
        returnReceiptPORepository.delete(ret);
    }

    @Override
    @Transactional(readOnly = true)
    public String generateNextCode() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "RPO-" + today + "-";
        List<String> matching = returnReceiptPORepository.findCodesMatchingPrefix(prefix);
        int maxSeq = 0;
        for (String c : matching) {
            try {
                String suffix = c.substring(prefix.length());
                int seq = Integer.parseInt(suffix);
                if (seq > maxSeq) maxSeq = seq;
            } catch (Exception ignored) {}
        }
        return String.format("%s%04d", prefix, maxSeq + 1);
    }

    private ReturnReceiptPODto mapToDto(ReturnReceiptPO r) {
        List<ReturnReceiptPOItemDto> items = r.getItems().stream()
                .map(i -> ReturnReceiptPOItemDto.builder()
                        .id(i.getId())
                        .productId(i.getProductId())
                        .code(i.getCode())
                        .barcode(i.getBarcode())
                        .description(i.getDescription())
                        .qty(i.getQty())
                        .uom(i.getUom())
                        .cost(i.getCost())
                        .total(i.getTotal())
                        .build())
                .collect(Collectors.toList());

        return ReturnReceiptPODto.builder()
                .id(r.getId())
                .returnPoCode(r.getReturnPoCode())
                .poCode(r.getPoCode())
                .receiptPoCode(r.getReceiptPoCode())
                .date(r.getDate())
                .supplier(r.getSupplier())
                .supplierId(r.getSupplierId())
                .amount(r.getAmount())
                .status(r.getStatus())
                .outlet(r.getOutlet())
                .username(r.getUsername())
                .reason(r.getReason())
                .reference(r.getReference())
                .note(r.getNote())
                .items(items)
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
