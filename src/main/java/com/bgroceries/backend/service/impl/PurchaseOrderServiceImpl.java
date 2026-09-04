package com.bgroceries.backend.service.impl;

import com.bgroceries.backend.dto.PurchaseOrderDto;
import com.bgroceries.backend.dto.PurchaseOrderItemDto;
import com.bgroceries.backend.entity.Purchase.PurchaseOrder;
import com.bgroceries.backend.entity.Purchase.PurchaseOrderItem;
import com.bgroceries.backend.repository.PurchaseOrderRepository;
import com.bgroceries.backend.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderDto> getAllPurchaseOrders(
            String search,
            String searchBy,
            LocalDate fromDate,
            LocalDate toDate,
            String outlet,
            String purchasePerson,
            String status
    ) {
        List<PurchaseOrder> all = purchaseOrderRepository.findAllByOrderByCreatedAtDesc();

        return all.stream()
                .filter(po -> {
                    // Search filter
                    if (search != null && !search.trim().isEmpty()) {
                        String s = search.trim().toLowerCase();
                        String by = searchBy != null ? searchBy.trim().toLowerCase() : "any";
                        boolean match = false;
                        switch (by) {
                            case "code":
                                match = po.getCode() != null && po.getCode().toLowerCase().contains(s);
                                break;
                            case "supplier":
                                match = po.getSupplier() != null && po.getSupplier().toLowerCase().contains(s);
                                break;
                            case "part number":
                            case "partnumber":
                                match = po.getItems().stream().anyMatch(i -> 
                                    i.getBarcode() != null && i.getBarcode().toLowerCase().contains(s));
                                break;
                            case "product code":
                            case "productcode":
                                match = po.getItems().stream().anyMatch(i -> 
                                    i.getItemCode() != null && i.getItemCode().toLowerCase().contains(s));
                                break;
                            case "reference":
                                match = (po.getReference() != null && po.getReference().toLowerCase().contains(s)) ||
                                        (po.getSoCode() != null && po.getSoCode().toLowerCase().contains(s));
                                break;
                            default: // any
                                match = (po.getCode() != null && po.getCode().toLowerCase().contains(s)) ||
                                        (po.getSupplier() != null && po.getSupplier().toLowerCase().contains(s)) ||
                                        (po.getPurchasePerson() != null && po.getPurchasePerson().toLowerCase().contains(s)) ||
                                        (po.getReference() != null && po.getReference().toLowerCase().contains(s)) ||
                                        (po.getSoCode() != null && po.getSoCode().toLowerCase().contains(s)) ||
                                        po.getItems().stream().anyMatch(i ->
                                            (i.getItemCode() != null && i.getItemCode().toLowerCase().contains(s)) ||
                                            (i.getBarcode() != null && i.getBarcode().toLowerCase().contains(s)) ||
                                            (i.getDescription() != null && i.getDescription().toLowerCase().contains(s))
                                        );
                                break;
                        }
                        if (!match) return false;
                    }

                    // Date range
                    if (fromDate != null && po.getDate() != null && po.getDate().isBefore(fromDate)) {
                        return false;
                    }
                    if (toDate != null && po.getDate() != null && po.getDate().isAfter(toDate)) {
                        return false;
                    }

                    // Outlet
                    if (outlet != null && !outlet.equalsIgnoreCase("all") && !outlet.equalsIgnoreCase("any")) {
                        if (po.getOutlet() == null || !po.getOutlet().equalsIgnoreCase(outlet)) {
                            return false;
                        }
                    }

                    // Purchase Person
                    if (purchasePerson != null && !purchasePerson.equalsIgnoreCase("all") && !purchasePerson.equalsIgnoreCase("any")) {
                        if (po.getPurchasePerson() == null || !po.getPurchasePerson().equalsIgnoreCase(purchasePerson)) {
                            return false;
                        }
                    }

                    // Status
                    if (status != null && !status.equalsIgnoreCase("all") && !status.equalsIgnoreCase("any")) {
                        if (po.getStatus() == null || !po.getStatus().equalsIgnoreCase(status)) {
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
    public PurchaseOrderDto getPurchaseOrderById(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new RuntimeException("PurchaseOrder not found with id: " + id));
        return mapToDto(po);
    }

    @Override
    @Transactional
    public PurchaseOrderDto createPurchaseOrder(PurchaseOrderDto dto) {
        String code = dto.getCode();
        if (code == null || code.isBlank() || purchaseOrderRepository.existsByCode(code)) {
            code = generateNextCode();
        }

        PurchaseOrder po = PurchaseOrder.builder()
                .code(code)
                .date(dto.getDate() != null ? dto.getDate() : LocalDate.now())
                .requireDate(dto.getRequireDate() != null ? dto.getRequireDate() : LocalDate.now().plusDays(7))
                .purchasePerson(dto.getPurchasePerson())
                .supplier(dto.getSupplier())
                .supplierId(dto.getSupplierId())
                .phone(dto.getPhone())
                .reference(dto.getReference())
                .voidedDate(dto.getVoidedDate())
                .soCode(dto.getSoCode())
                .status(dto.getStatus() != null && !dto.getStatus().isBlank() ? dto.getStatus().toUpperCase() : "OPEN")
                .username(dto.getUsername() != null ? dto.getUsername() : "Badmin")
                .outlet(dto.getOutlet() != null ? dto.getOutlet() : "Main Supermarket")
                .paymentTerm(dto.getPaymentTerm())
                .shipmentMethod(dto.getShipmentMethod())
                .templateName(dto.getTemplateName())
                .note(dto.getNote())
                .discountPercent(dto.getDiscountPercent() != null ? dto.getDiscountPercent() : 0.0)
                .discountAmount(dto.getDiscountAmount() != null ? dto.getDiscountAmount() : 0.0)
                .taxAmount(dto.getTaxAmount() != null ? dto.getTaxAmount() : 0.0)
                .billingAddress(dto.getBillingAddress())
                .shippingAddress(dto.getShippingAddress())
                .carrier(dto.getCarrier())
                .trackingNumber(dto.getTrackingNumber())
                .build();

        double subTotal = 0.0;
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (PurchaseOrderItemDto itemDto : dto.getItems()) {
                double qty = itemDto.getQty() != null ? itemDto.getQty() : 1.0;
                double cost = itemDto.getCost() != null ? itemDto.getCost() : 0.0;
                double discount = itemDto.getDiscount() != null ? itemDto.getDiscount() : 0.0;
                double lineTotal = (qty * cost) - discount;
                if (lineTotal < 0) lineTotal = 0;

                PurchaseOrderItem item = PurchaseOrderItem.builder()
                        .productId(itemDto.getProductId())
                        .itemCode(itemDto.getItemCode())
                        .barcode(itemDto.getBarcode())
                        .description(itemDto.getDescription())
                        .description2(itemDto.getDescription2())
                        .productGroup(itemDto.getProductGroup())
                        .onhand(itemDto.getOnhand() != null ? itemDto.getOnhand() : 0)
                        .suggestQty(itemDto.getSuggestQty() != null ? itemDto.getSuggestQty() : 0)
                        .qty(qty)
                        .cost(cost)
                        .discount(discount)
                        .uom(itemDto.getUom() != null ? itemDto.getUom() : "Pcs")
                        .total(lineTotal)
                        .build();

                po.addItem(item);
                subTotal += lineTotal;
            }
        }

        po.setSubAmount(subTotal);
        double totalDiscount = po.getDiscountAmount() != null && po.getDiscountAmount() > 0 
                ? po.getDiscountAmount() 
                : (subTotal * (po.getDiscountPercent() != null ? po.getDiscountPercent() : 0.0) / 100.0);
        double grandTotal = subTotal - totalDiscount + (po.getTaxAmount() != null ? po.getTaxAmount() : 0.0);
        if (grandTotal < 0) grandTotal = 0;
        po.setGrandTotal(grandTotal);
        po.setBalance(grandTotal);

        PurchaseOrder saved = purchaseOrderRepository.save(po);
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public PurchaseOrderDto updatePurchaseOrder(Long id, PurchaseOrderDto dto) {
        PurchaseOrder po = purchaseOrderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new RuntimeException("PurchaseOrder not found with id: " + id));

        po.setDate(dto.getDate());
        po.setRequireDate(dto.getRequireDate());
        po.setPurchasePerson(dto.getPurchasePerson());
        po.setSupplier(dto.getSupplier());
        po.setSupplierId(dto.getSupplierId());
        po.setPhone(dto.getPhone());
        po.setReference(dto.getReference());
        po.setVoidedDate(dto.getVoidedDate());
        po.setSoCode(dto.getSoCode());
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            po.setStatus(dto.getStatus().toUpperCase());
        }
        po.setOutlet(dto.getOutlet());
        po.setPaymentTerm(dto.getPaymentTerm());
        po.setShipmentMethod(dto.getShipmentMethod());
        po.setTemplateName(dto.getTemplateName());
        po.setNote(dto.getNote());
        po.setDiscountPercent(dto.getDiscountPercent());
        po.setDiscountAmount(dto.getDiscountAmount());
        po.setTaxAmount(dto.getTaxAmount());
        po.setBillingAddress(dto.getBillingAddress());
        po.setShippingAddress(dto.getShippingAddress());
        po.setCarrier(dto.getCarrier());
        po.setTrackingNumber(dto.getTrackingNumber());

        po.getItems().clear();
        double subTotal = 0.0;
        if (dto.getItems() != null) {
            for (PurchaseOrderItemDto itemDto : dto.getItems()) {
                double qty = itemDto.getQty() != null ? itemDto.getQty() : 1.0;
                double cost = itemDto.getCost() != null ? itemDto.getCost() : 0.0;
                double discount = itemDto.getDiscount() != null ? itemDto.getDiscount() : 0.0;
                double lineTotal = (qty * cost) - discount;
                if (lineTotal < 0) lineTotal = 0;

                PurchaseOrderItem item = PurchaseOrderItem.builder()
                        .productId(itemDto.getProductId())
                        .itemCode(itemDto.getItemCode())
                        .barcode(itemDto.getBarcode())
                        .description(itemDto.getDescription())
                        .description2(itemDto.getDescription2())
                        .productGroup(itemDto.getProductGroup())
                        .onhand(itemDto.getOnhand() != null ? itemDto.getOnhand() : 0)
                        .suggestQty(itemDto.getSuggestQty() != null ? itemDto.getSuggestQty() : 0)
                        .qty(qty)
                        .cost(cost)
                        .discount(discount)
                        .uom(itemDto.getUom() != null ? itemDto.getUom() : "Pcs")
                        .total(lineTotal)
                        .build();

                po.addItem(item);
                subTotal += lineTotal;
            }
        }

        po.setSubAmount(subTotal);
        double totalDiscount = po.getDiscountAmount() != null && po.getDiscountAmount() > 0 
                ? po.getDiscountAmount() 
                : (subTotal * (po.getDiscountPercent() != null ? po.getDiscountPercent() : 0.0) / 100.0);
        double grandTotal = subTotal - totalDiscount + (po.getTaxAmount() != null ? po.getTaxAmount() : 0.0);
        if (grandTotal < 0) grandTotal = 0;
        po.setGrandTotal(grandTotal);

        PurchaseOrder updated = purchaseOrderRepository.save(po);
        return mapToDto(updated);
    }

    @Override
    @Transactional
    public PurchaseOrderDto updateStatus(Long id, String status) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PurchaseOrder not found with id: " + id));
        po.setStatus(status.toUpperCase());
        if ("VOIDED".equalsIgnoreCase(status)) {
            po.setVoidedDate(LocalDate.now());
        }
        return mapToDto(purchaseOrderRepository.save(po));
    }

    @Override
    @Transactional
    public void deletePurchaseOrder(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PurchaseOrder not found with id: " + id));
        purchaseOrderRepository.delete(po);
    }

    @Override
    @Transactional(readOnly = true)
    public String generateNextCode() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "PO-" + today + "-";
        List<String> matching = purchaseOrderRepository.findCodesMatchingPrefix(prefix);
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

    private PurchaseOrderDto mapToDto(PurchaseOrder po) {
        List<PurchaseOrderItemDto> items = po.getItems().stream()
                .map(i -> PurchaseOrderItemDto.builder()
                        .id(i.getId())
                        .productId(i.getProductId())
                        .itemCode(i.getItemCode())
                        .barcode(i.getBarcode())
                        .description(i.getDescription())
                        .description2(i.getDescription2())
                        .productGroup(i.getProductGroup())
                        .onhand(i.getOnhand())
                        .suggestQty(i.getSuggestQty())
                        .qty(i.getQty())
                        .cost(i.getCost())
                        .discount(i.getDiscount())
                        .uom(i.getUom())
                        .total(i.getTotal())
                        .build())
                .collect(Collectors.toList());

        return PurchaseOrderDto.builder()
                .id(po.getId())
                .code(po.getCode())
                .date(po.getDate())
                .requireDate(po.getRequireDate())
                .purchasePerson(po.getPurchasePerson())
                .supplier(po.getSupplier())
                .supplierId(po.getSupplierId())
                .phone(po.getPhone())
                .grandTotal(po.getGrandTotal())
                .balance(po.getBalance())
                .reference(po.getReference())
                .voidedDate(po.getVoidedDate())
                .soCode(po.getSoCode())
                .status(po.getStatus())
                .username(po.getUsername())
                .outlet(po.getOutlet())
                .paymentTerm(po.getPaymentTerm())
                .shipmentMethod(po.getShipmentMethod())
                .templateName(po.getTemplateName())
                .note(po.getNote())
                .subAmount(po.getSubAmount())
                .discountPercent(po.getDiscountPercent())
                .discountAmount(po.getDiscountAmount())
                .taxAmount(po.getTaxAmount())
                .billingAddress(po.getBillingAddress())
                .shippingAddress(po.getShippingAddress())
                .carrier(po.getCarrier())
                .trackingNumber(po.getTrackingNumber())
                .items(items)
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }
}
