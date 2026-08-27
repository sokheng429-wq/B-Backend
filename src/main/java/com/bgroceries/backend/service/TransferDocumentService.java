package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.TransferDocumentDto;
import com.bgroceries.backend.entity.Product;
import com.bgroceries.backend.entity.TransferDocument;
import com.bgroceries.backend.entity.TransferLine;
import com.bgroceries.backend.exception.NotFoundException;
import com.bgroceries.backend.repository.ProductRepository;
import com.bgroceries.backend.repository.TransferDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferDocumentService {

    private static final DateTimeFormatter CODE_DATE = DateTimeFormatter.ofPattern("yyMMdd");

    private final TransferDocumentRepository transferDocumentRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<TransferDocumentDto> getAll(TransferDocument.DocType docType, String status) {
        List<TransferDocument> docs = transferDocumentRepository.filterTransfers(docType, normalize(status));
        return docs.stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public TransferDocumentDto getById(Long id) {
        TransferDocument doc = transferDocumentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transfer document not found: " + id));
        return toDto(doc);
    }

    @Transactional
    public TransferDocumentDto create(TransferDocumentDto dto) {
        String fromOutlet = dto.getFromOutlet() != null ? dto.getFromOutlet() : dto.getRequestOutlet();
        String fromLocation = dto.getFromLocation() != null ? dto.getFromLocation() : dto.getRequestLocation();
        String transferType = dto.getTransferType() != null ? dto.getTransferType() : dto.getRequestTransferType();

        TransferDocument doc = TransferDocument.builder()
                .code(resolveCode(dto))
                .docType(dto.getDocType() != null ? dto.getDocType() : TransferDocument.DocType.REQUEST)
                .transferDate(dto.getTransferDate() != null ? dto.getTransferDate() : LocalDate.now())
                .requiredDate(dto.getRequiredDate())
                .requestTransferDate(dto.getRequestTransferDate() != null ? dto.getRequestTransferDate() : dto.getTransferDate())
                .fromOutlet(normalize(fromOutlet))
                .fromLocation(normalize(fromLocation))
                .toOutlet(normalize(dto.getToOutlet()))
                .toLocation(normalize(dto.getToLocation()))
                .transferType(normalize(transferType))
                .reference(normalize(dto.getReference()))
                .templateName(normalize(dto.getTemplateName()))
                .carrier(normalize(dto.getCarrier()))
                .trackingNumber(normalize(dto.getTrackingNumber()))
                .dispatchNote(normalize(dto.getDispatchNote()))
                .status(normalize(dto.getStatus()))
                .userName(normalize(dto.getUserName()))
                .build();

        BigDecimal totalQty = BigDecimal.ZERO;
        if (dto.getLines() != null) {
            for (TransferDocumentDto.Line lineDto : dto.getLines()) {
                Product product = null;
                if (lineDto.getProductId() != null) {
                    product = productRepository.findById(lineDto.getProductId()).orElse(null);
                }

                BigDecimal qty = lineDto.getQty() != null ? lineDto.getQty() : BigDecimal.ZERO;
                totalQty = totalQty.add(qty);

                TransferLine line = TransferLine.builder()
                        .transferDocument(doc)
                        .product(product)
                        .code(lineDto.getCode() != null ? lineDto.getCode() : (product != null ? product.getCode() : null))
                        .barCode(lineDto.getBarCode() != null ? lineDto.getBarCode() : (product != null ? product.getBarCode() : null))
                        .name(lineDto.getName() != null ? lineDto.getName() : (product != null ? product.getName() : null))
                        .nameKh(lineDto.getNameKh() != null ? lineDto.getNameKh() : (product != null ? product.getNameKh() : null))
                        .uom(lineDto.getUom() != null ? lineDto.getUom() : (product != null ? product.getUom() : null))
                        .onHand(lineDto.getOnHand() != null ? lineDto.getOnHand() : (product != null ? product.getOnHand() : BigDecimal.ZERO))
                        .qty(qty)
                        .unitCost(lineDto.getUnitCost() != null ? lineDto.getUnitCost() : (product != null ? product.getAverageCost() : BigDecimal.ZERO))
                        .lineTotal(lineDto.getLineTotal() != null ? lineDto.getLineTotal() : qty.multiply(lineDto.getUnitCost() != null ? lineDto.getUnitCost() : BigDecimal.ZERO))
                        .build();

                doc.getLines().add(line);
            }
        }

        doc.setTotalQty(totalQty);
        return toDto(transferDocumentRepository.save(doc));
    }

    @Transactional
    public TransferDocumentDto updateStatus(Long id, TransferDocumentDto updateDto) {
        TransferDocument doc = transferDocumentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transfer document not found: " + id));

        if (updateDto.getStatus() != null && !updateDto.getStatus().isBlank()) {
            doc.setStatus(updateDto.getStatus().trim().toUpperCase());
        }
        if (updateDto.getCarrier() != null) doc.setCarrier(normalize(updateDto.getCarrier()));
        if (updateDto.getTrackingNumber() != null) doc.setTrackingNumber(normalize(updateDto.getTrackingNumber()));
        if (updateDto.getDispatchNote() != null) doc.setDispatchNote(normalize(updateDto.getDispatchNote()));

        return toDto(transferDocumentRepository.save(doc));
    }

    @Transactional
    public int bulkShip(List<Long> ids, String carrier, String dispatchNote) {
        List<TransferDocument> targets;
        if (ids != null && !ids.isEmpty()) {
            targets = transferDocumentRepository.findAllById(ids);
        } else {
            targets = transferDocumentRepository.findByDocTypeAndStatusOrderByCreatedAtDesc(TransferDocument.DocType.REQUEST, "PENDING");
        }

        int count = 0;
        for (TransferDocument doc : targets) {
            if ("PENDING".equalsIgnoreCase(doc.getStatus())) {
                doc.setStatus("IN-TRANSIT");
                if (carrier != null && !carrier.isBlank()) doc.setCarrier(carrier.trim());
                if (dispatchNote != null && !dispatchNote.isBlank()) doc.setDispatchNote(dispatchNote.trim());
                transferDocumentRepository.save(doc);
                count++;
            }
        }
        return count;
    }

    @Transactional
    public void delete(Long id) {
        TransferDocument doc = transferDocumentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transfer document not found: " + id));
        transferDocumentRepository.delete(doc);
    }

    private String resolveCode(TransferDocumentDto dto) {
        if (dto.getCode() != null && !dto.getCode().isBlank()) {
            String candidate = dto.getCode().trim();
            if (transferDocumentRepository.findByCode(candidate).isEmpty()) {
                return candidate;
            }
        }

        String prefix = dto.getDocType() == TransferDocument.DocType.TRANSFER ? "TF" : "TR";
        String datePart = LocalDate.now().format(CODE_DATE);
        int seq = 1;
        String candidate = prefix + "-" + datePart + "-" + String.format("%04d", seq);
        while (transferDocumentRepository.findByCode(candidate).isPresent()) {
            seq++;
            candidate = prefix + "-" + datePart + "-" + String.format("%04d", seq);
        }
        return candidate;
    }

    private String normalize(String val) {
        if (val == null) return null;
        String t = val.trim();
        return t.isEmpty() ? null : t;
    }

    private TransferDocumentDto toDto(TransferDocument doc) {
        List<TransferDocumentDto.Line> lines = new ArrayList<>();
        if (doc.getLines() != null) {
            for (TransferLine l : doc.getLines()) {
                lines.add(TransferDocumentDto.Line.builder()
                        .id(l.getId())
                        .productId(l.getProduct() != null ? l.getProduct().getId() : null)
                        .code(l.getCode())
                        .barCode(l.getBarCode())
                        .name(l.getName())
                        .nameKh(l.getNameKh())
                        .uom(l.getUom())
                        .onHand(l.getOnHand())
                        .qty(l.getQty())
                        .unitCost(l.getUnitCost())
                        .lineTotal(l.getLineTotal())
                        .build());
            }
        }

        return TransferDocumentDto.builder()
                .id(doc.getId())
                .code(doc.getCode())
                .docType(doc.getDocType())
                .transferDate(doc.getTransferDate())
                .requiredDate(doc.getRequiredDate())
                .requestTransferDate(doc.getRequestTransferDate())
                .fromOutlet(doc.getFromOutlet())
                .fromLocation(doc.getFromLocation())
                .requestOutlet(doc.getFromOutlet())
                .requestLocation(doc.getFromLocation())
                .toOutlet(doc.getToOutlet())
                .toLocation(doc.getToLocation())
                .transferType(doc.getTransferType())
                .requestTransferType(doc.getTransferType())
                .reference(doc.getReference())
                .templateName(doc.getTemplateName())
                .carrier(doc.getCarrier())
                .trackingNumber(doc.getTrackingNumber())
                .dispatchNote(doc.getDispatchNote())
                .status(doc.getStatus())
                .userName(doc.getUserName())
                .totalQty(doc.getTotalQty())
                .lines(lines)
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }
}