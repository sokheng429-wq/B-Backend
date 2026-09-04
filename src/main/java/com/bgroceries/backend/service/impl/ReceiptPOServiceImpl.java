package com.bgroceries.backend.service.impl;

import com.bgroceries.backend.dto.ReceiptPODto;
import com.bgroceries.backend.dto.ReceiptPOItemDto;
import com.bgroceries.backend.entity.Purchase.ReceiptPO;
import com.bgroceries.backend.entity.Purchase.ReceiptPOItem;
import com.bgroceries.backend.repository.ReceiptPORepository;
import com.bgroceries.backend.service.ReceiptPOService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReceiptPOServiceImpl implements ReceiptPOService {

    private final ReceiptPORepository receiptPORepository;

    @Override
    @Transactional(readOnly = true)
    public List<ReceiptPODto> getAllReceiptPOs(
            String search,
            String searchBy,
            LocalDate fromDate,
            LocalDate toDate,
            String outlet,
            String status
    ) {
        List<ReceiptPO> all = receiptPORepository.findAllByOrderByCreatedAtDesc();

        return all.stream()
                .filter(rec -> {
                    // Search condition
                    if (search != null && !search.trim().isEmpty()) {
                        String s = search.trim().toLowerCase();
                        String by = searchBy != null ? searchBy.trim().toLowerCase() : "any";
                        boolean match = false;
                        switch (by) {
                            case "receipt po code":
                            case "receiptpocode":
                            case "code":
                                match = rec.getReceiptPoCode() != null && rec.getReceiptPoCode().toLowerCase().contains(s);
                                break;
                            case "po code":
                            case "pocode":
                                match = rec.getPoCode() != null && rec.getPoCode().toLowerCase().contains(s);
                                break;
                            case "supplier":
                                match = rec.getSupplier() != null && rec.getSupplier().toLowerCase().contains(s);
                                break;
                            default: // any
                                match = (rec.getReceiptPoCode() != null && rec.getReceiptPoCode().toLowerCase().contains(s)) ||
                                        (rec.getPoCode() != null && rec.getPoCode().toLowerCase().contains(s)) ||
                                        (rec.getSupplier() != null && rec.getSupplier().toLowerCase().contains(s)) ||
                                        (rec.getOutlet() != null && rec.getOutlet().toLowerCase().contains(s)) ||
                                        (rec.getShipment() != null && rec.getShipment().toLowerCase().contains(s)) ||
                                        (rec.getUsername() != null && rec.getUsername().toLowerCase().contains(s));
                                break;
                        }
                        if (!match) return false;
                    }

                    // Date range
                    if (fromDate != null && rec.getDate() != null && rec.getDate().isBefore(fromDate)) {
                        return false;
                    }
                    if (toDate != null && rec.getDate() != null && rec.getDate().isAfter(toDate)) {
                        return false;
                    }

                    // Outlet
                    if (outlet != null && !outlet.equalsIgnoreCase("all") && !outlet.equalsIgnoreCase("any")) {
                        if (rec.getOutlet() == null || !rec.getOutlet().equalsIgnoreCase(outlet)) {
                            return false;
                        }
                    }

                    // Status
                    if (status != null && !status.equalsIgnoreCase("all") && !status.equalsIgnoreCase("any")) {
                        if (rec.getStatus() == null || !rec.getStatus().equalsIgnoreCase(status)) {
                            return false;
                        }
                    }

                    return true;
                })
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReceiptPODto getReceiptPOById(Long id) {
        ReceiptPO rec = receiptPORepository.findByIdWithItems(id)
                .orElseThrow(() -> new RuntimeException("ReceiptPO not found with id: " + id));
        return mapToDto(rec);
    }

    @Override
    @Transactional
    public ReceiptPODto createReceiptPO(ReceiptPODto dto) {
        String code = dto.getReceiptPoCode();
        if (code == null || code.isBlank() || receiptPORepository.existsByReceiptPoCode(code)) {
            code = generateNextCode();
        }

        ReceiptPO rec = ReceiptPO.builder()
                .receiptPoCode(code)
                .poCode(dto.getPoCode())
                .date(dto.getDate() != null ? dto.getDate() : LocalDate.now())
                .supplier(dto.getSupplier())
                .supplierId(dto.getSupplierId())
                .balance(dto.getBalance() != null ? dto.getBalance() : 0.0)
                .status(dto.getStatus() != null && !dto.getStatus().isBlank() ? dto.getStatus().toUpperCase() : "RECEIVED")
                .outlet(dto.getOutlet() != null ? dto.getOutlet() : "Main Supermarket")
                .shipment(dto.getShipment() != null ? dto.getShipment() : "Standard Freight")
                .username(dto.getUsername() != null ? dto.getUsername() : "Badmin")
                .reference(dto.getReference())
                .note(dto.getNote())
                .build();

        double totalAmount = 0.0;
        double totalQty = 0.0;

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (ReceiptPOItemDto itemDto : dto.getItems()) {
                double q = itemDto.getQty() != null ? itemDto.getQty() : 1.0;
                double c = itemDto.getCost() != null ? itemDto.getCost() : 0.0;
                double lineTotal = q * c;

                ReceiptPOItem item = ReceiptPOItem.builder()
                        .productId(itemDto.getProductId())
                        .code(itemDto.getCode())
                        .barcode(itemDto.getBarcode())
                        .description(itemDto.getDescription())
                        .qty(q)
                        .uom(itemDto.getUom() != null ? itemDto.getUom() : "Pcs")
                        .cost(c)
                        .total(lineTotal)
                        .build();

                rec.addItem(item);
                totalAmount += lineTotal;
                totalQty += q;
            }
        }

        rec.setAmount(dto.getAmount() != null && dto.getAmount() > 0 ? dto.getAmount() : totalAmount);
        rec.setQty(dto.getQty() != null && dto.getQty() > 0 ? dto.getQty() : totalQty);

        ReceiptPO saved = receiptPORepository.save(rec);
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public ReceiptPODto updateReceiptPO(Long id, ReceiptPODto dto) {
        ReceiptPO rec = receiptPORepository.findByIdWithItems(id)
                .orElseThrow(() -> new RuntimeException("ReceiptPO not found with id: " + id));

        rec.setPoCode(dto.getPoCode());
        rec.setDate(dto.getDate());
        rec.setSupplier(dto.getSupplier());
        rec.setSupplierId(dto.getSupplierId());
        rec.setBalance(dto.getBalance());
        if (dto.getFreightAmount() != null) {
            rec.setFreightAmount(dto.getFreightAmount());
        }
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            rec.setStatus(dto.getStatus().toUpperCase());
        }
        rec.setOutlet(dto.getOutlet());
        rec.setShipment(dto.getShipment());
        rec.setReference(dto.getReference());
        rec.setNote(dto.getNote());

        rec.getItems().clear();
        double totalAmount = 0.0;
        double totalQty = 0.0;

        if (dto.getItems() != null) {
            for (ReceiptPOItemDto itemDto : dto.getItems()) {
                double q = itemDto.getQty() != null ? itemDto.getQty() : 1.0;
                double c = itemDto.getCost() != null ? itemDto.getCost() : 0.0;
                double lineTotal = q * c;

                ReceiptPOItem item = ReceiptPOItem.builder()
                        .productId(itemDto.getProductId())
                        .code(itemDto.getCode())
                        .barcode(itemDto.getBarcode())
                        .description(itemDto.getDescription())
                        .qty(q)
                        .uom(itemDto.getUom() != null ? itemDto.getUom() : "Pcs")
                        .cost(c)
                        .total(lineTotal)
                        .build();

                rec.addItem(item);
                totalAmount += lineTotal;
                totalQty += q;
            }
        }

        rec.setAmount(dto.getAmount() != null && dto.getAmount() > 0 ? dto.getAmount() : totalAmount);
        rec.setQty(dto.getQty() != null && dto.getQty() > 0 ? dto.getQty() : totalQty);

        ReceiptPO updated = receiptPORepository.save(rec);
        return mapToDto(updated);
    }

    @Override
    @Transactional
    public ReceiptPODto updateStatus(Long id, String status) {
        ReceiptPO rec = receiptPORepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ReceiptPO not found with id: " + id));
        rec.setStatus(status.toUpperCase());
        return mapToDto(receiptPORepository.save(rec));
    }

    @Override
    @Transactional
    public void deleteReceiptPO(Long id) {
        ReceiptPO rec = receiptPORepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ReceiptPO not found with id: " + id));
        receiptPORepository.delete(rec);
    }

    @Override
    @Transactional(readOnly = true)
    public String generateNextCode() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "REC-" + today + "-";
        List<String> matching = receiptPORepository.findCodesMatchingPrefix(prefix);
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

    private ReceiptPODto mapToDto(ReceiptPO r) {
        List<ReceiptPOItemDto> items = r.getItems().stream()
                .map(i -> ReceiptPOItemDto.builder()
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

        return ReceiptPODto.builder()
                .id(r.getId())
                .receiptPoCode(r.getReceiptPoCode())
                .poCode(r.getPoCode())
                .date(r.getDate())
                .supplier(r.getSupplier())
                .supplierId(r.getSupplierId())
                .balance(r.getBalance())
                .amount(r.getAmount())
                .freightAmount(r.getFreightAmount() != null ? r.getFreightAmount() : 0.0)
                .qty(r.getQty())
                .status(r.getStatus())
                .outlet(r.getOutlet())
                .shipment(r.getShipment())
                .username(r.getUsername())
                .reference(r.getReference())
                .note(r.getNote())
                .items(items)
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
