package com.bgroceries.backend.service.impl;

import com.bgroceries.backend.dto.SaleInvoiceDto;
import com.bgroceries.backend.dto.SaleInvoiceItemDto;
import com.bgroceries.backend.dto.SaleInvoicePaymentDto;
import com.bgroceries.backend.dto.SaleInvoiceStatsDto;
import com.bgroceries.backend.entity.Sale.Customer;
import com.bgroceries.backend.entity.Sale.SaleInvoice;
import com.bgroceries.backend.entity.Sale.SaleInvoiceItem;
import com.bgroceries.backend.entity.Sale.SaleInvoicePayment;
import com.bgroceries.backend.repository.CustomerRepository;
import com.bgroceries.backend.repository.SaleInvoicePaymentRepository;
import com.bgroceries.backend.repository.SaleInvoiceRepository;
import com.bgroceries.backend.service.SaleInvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SaleInvoiceServiceImpl implements SaleInvoiceService {

    private final SaleInvoiceRepository saleInvoiceRepository;
    private final SaleInvoicePaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;

    private static final BigDecimal DEFAULT_EXCHANGE_RATE = new BigDecimal("4100.00");

    @Override
    @Transactional(readOnly = true)
    public List<SaleInvoiceDto> getAllInvoices(String search, String searchBy, String status, LocalDate startDate, LocalDate endDate) {
        List<SaleInvoice> list;

        if (searchBy != null && !searchBy.equalsIgnoreCase("any") && search != null && !search.isBlank()) {
            list = saleInvoiceRepository.searchByField(searchBy, search.trim());
        } else {
            list = saleInvoiceRepository.searchInvoices(
                    search != null && !search.isBlank() ? search.trim() : null,
                    status != null && !status.equalsIgnoreCase("ALL") ? status : null,
                    startDate,
                    endDate
            );
        }

        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SaleInvoiceDto getInvoiceById(Long id) {
        SaleInvoice invoice = saleInvoiceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Sale invoice not found with ID: " + id));
        return toDto(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public SaleInvoiceDto getInvoiceByCode(String code) {
        SaleInvoice invoice = saleInvoiceRepository.findByInvoiceCode(code)
                .orElseThrow(() -> new NoSuchElementException("Sale invoice not found with code: " + code));
        return toDto(invoice);
    }

    @Override
    @Transactional
    public SaleInvoiceDto createInvoice(SaleInvoiceDto dto) {
        String code = (dto.getInvoiceCode() != null && !dto.getInvoiceCode().isBlank())
                ? dto.getInvoiceCode().trim()
                : generateNextInvoiceCode();

        BigDecimal exchangeRate = dto.getExchangeRate() != null && dto.getExchangeRate().compareTo(BigDecimal.ZERO) > 0
                ? dto.getExchangeRate()
                : DEFAULT_EXCHANGE_RATE;

        SaleInvoice invoice = SaleInvoice.builder()
                .invoiceCode(code)
                .invoiceDate(dto.getInvoiceDate() != null ? dto.getInvoiceDate() : LocalDate.now())
                .dueDate(dto.getDueDate())
                .soCode(dto.getSoCode())
                .customerId(dto.getCustomerId())
                .customerName(dto.getCustomerName())
                .customerPhone(dto.getCustomerPhone())
                .customerAddress(dto.getCustomerAddress())
                .salesperson(dto.getSalesperson())
                .paymentTerm(dto.getPaymentTerm())
                .outlet(dto.getOutlet())
                .location(dto.getLocation())
                .templateName(dto.getTemplateName())
                .status(dto.getStatus() != null ? dto.getStatus() : "UNPAID")
                .subTotal(dto.getSubTotal() != null ? dto.getSubTotal() : BigDecimal.ZERO)
                .discountPercent(dto.getDiscountPercent() != null ? dto.getDiscountPercent() : BigDecimal.ZERO)
                .discountAmount(dto.getDiscountAmount() != null ? dto.getDiscountAmount() : BigDecimal.ZERO)
                .taxAmount(dto.getTaxAmount() != null ? dto.getTaxAmount() : BigDecimal.ZERO)
                .taxPercent(dto.getTaxPercent() != null ? dto.getTaxPercent() : BigDecimal.ZERO)
                .markupAmount(dto.getMarkupAmount() != null ? dto.getMarkupAmount() : BigDecimal.ZERO)
                .grandTotal(dto.getGrandTotal() != null ? dto.getGrandTotal() : BigDecimal.ZERO)
                .paidAmount(dto.getPaidAmount() != null ? dto.getPaidAmount() : BigDecimal.ZERO)
                .balance(dto.getBalance() != null ? dto.getBalance() : BigDecimal.ZERO)
                .exchangeRate(exchangeRate)
                .grandTotalKhmer(dto.getGrandTotalKhmer() != null ? dto.getGrandTotalKhmer() : BigDecimal.ZERO)
                .barcode(dto.getBarcode())
                .username(dto.getUsername())
                .note(dto.getNote())
                .paymentType(dto.getPaymentType())
                .billingName(dto.getBillingName())
                .billingPhone(dto.getBillingPhone())
                .billingEmail(dto.getBillingEmail())
                .billingAddress(dto.getBillingAddress())
                .billingCity(dto.getBillingCity())
                .billingTaxNo(dto.getBillingTaxNo())
                .shippingRecipient(dto.getShippingRecipient())
                .shippingPhone(dto.getShippingPhone())
                .shippingAddress(dto.getShippingAddress())
                .shippingMethod(dto.getShippingMethod())
                .trackingNo(dto.getTrackingNo())
                .lines(new ArrayList<>())
                .payments(new ArrayList<>())
                .build();

        // Calculate line items
        if (dto.getLines() != null && !dto.getLines().isEmpty()) {
            for (SaleInvoiceItemDto itemDto : dto.getLines()) {
                BigDecimal qty = itemDto.getQty() != null ? itemDto.getQty() : BigDecimal.ONE;
                BigDecimal unitPrice = itemDto.getUnitPrice() != null ? itemDto.getUnitPrice() : BigDecimal.ZERO;
                BigDecimal discount = itemDto.getDiscount() != null ? itemDto.getDiscount() : BigDecimal.ZERO;
                BigDecimal lineTotal = qty.multiply(unitPrice).subtract(discount);
                if (lineTotal.compareTo(BigDecimal.ZERO) < 0) lineTotal = BigDecimal.ZERO;

                SaleInvoiceItem item = SaleInvoiceItem.builder()
                        .saleInvoice(invoice)
                        .productId(itemDto.getProductId())
                        .productCode(itemDto.getProductCode())
                        .description(itemDto.getDescription() != null ? itemDto.getDescription() : "Product")
                        .qty(qty)
                        .unitPrice(unitPrice)
                        .discount(discount)
                        .uom(itemDto.getUom())
                        .totalPrice(itemDto.getTotalPrice() != null ? itemDto.getTotalPrice() : lineTotal)
                        .build();

                invoice.getLines().add(item);
            }
        }

        // Auto-calculate grand total if zero
        if (invoice.getGrandTotal().compareTo(BigDecimal.ZERO) == 0 && !invoice.getLines().isEmpty()) {
            BigDecimal calcSub = invoice.getLines().stream()
                    .map(SaleInvoiceItem::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            invoice.setSubTotal(calcSub);

            BigDecimal disc = invoice.getDiscountAmount();
            if (invoice.getDiscountPercent() != null && invoice.getDiscountPercent().compareTo(BigDecimal.ZERO) > 0) {
                disc = calcSub.multiply(invoice.getDiscountPercent()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                invoice.setDiscountAmount(disc);
            }

            BigDecimal grand = calcSub.subtract(disc).add(invoice.getTaxAmount()).add(invoice.getMarkupAmount());
            if (grand.compareTo(BigDecimal.ZERO) < 0) grand = BigDecimal.ZERO;
            invoice.setGrandTotal(grand);
        }

        if (invoice.getGrandTotalKhmer().compareTo(BigDecimal.ZERO) == 0) {
            invoice.setGrandTotalKhmer(invoice.getGrandTotal().multiply(exchangeRate));
        }

        // Check payments
        BigDecimal totalPaid = BigDecimal.ZERO;
        if (dto.getPayments() != null && !dto.getPayments().isEmpty()) {
            for (SaleInvoicePaymentDto pDto : dto.getPayments()) {
                BigDecimal pAmt = pDto.getAmountDollar() != null ? pDto.getAmountDollar() : BigDecimal.ZERO;
                totalPaid = totalPaid.add(pAmt);

                SaleInvoicePayment p = SaleInvoicePayment.builder()
                        .saleInvoice(invoice)
                        .paymentDate(pDto.getPaymentDate() != null ? pDto.getPaymentDate() : LocalDateTime.now())
                        .amountDollar(pAmt)
                        .amountKhmer(pDto.getAmountKhmer() != null ? pDto.getAmountKhmer() : pAmt.multiply(exchangeRate))
                        .paymentType(pDto.getPaymentType() != null ? pDto.getPaymentType() : "CASH")
                        .reference(pDto.getReference())
                        .note(pDto.getNote())
                        .receivedBy(pDto.getReceivedBy() != null ? pDto.getReceivedBy() : dto.getUsername())
                        .build();

                invoice.getPayments().add(p);
            }
        } else if (dto.getPaidAmount() != null && dto.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            totalPaid = dto.getPaidAmount();
            SaleInvoicePayment p = SaleInvoicePayment.builder()
                    .saleInvoice(invoice)
                    .paymentDate(LocalDateTime.now())
                    .amountDollar(totalPaid)
                    .amountKhmer(totalPaid.multiply(exchangeRate))
                    .paymentType(dto.getPaymentType() != null ? dto.getPaymentType() : "CASH")
                    .receivedBy(dto.getUsername())
                    .build();
            invoice.getPayments().add(p);
        }

        invoice.setPaidAmount(totalPaid);
        BigDecimal balance = invoice.getGrandTotal().subtract(totalPaid);
        if (balance.compareTo(BigDecimal.ZERO) < 0) balance = BigDecimal.ZERO;
        invoice.setBalance(balance);

        // Determine Status
        if ("CREDIT".equalsIgnoreCase(dto.getPaymentType()) || "Credit".equalsIgnoreCase(dto.getPaymentTerm())) {
            invoice.setStatus(balance.compareTo(BigDecimal.ZERO) == 0 ? "PAID" : "CREDIT");
        } else if (balance.compareTo(BigDecimal.ZERO) == 0 && invoice.getGrandTotal().compareTo(BigDecimal.ZERO) > 0) {
            invoice.setStatus("PAID");
        } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0 && balance.compareTo(BigDecimal.ZERO) > 0) {
            invoice.setStatus("PARTIAL");
        } else {
            invoice.setStatus("UNPAID");
        }

        SaleInvoice saved = saleInvoiceRepository.save(invoice);

        // Update customer balance if attached
        if (saved.getCustomerId() != null && saved.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            customerRepository.findById(saved.getCustomerId()).ifPresent(c -> {
                BigDecimal cur = c.getBalance() != null ? c.getBalance() : BigDecimal.ZERO;
                c.setBalance(cur.add(saved.getBalance()));
                customerRepository.save(c);
            });
        }

        return toDto(saved);
    }

    @Override
    @Transactional
    public SaleInvoiceDto updateInvoice(Long id, SaleInvoiceDto dto) {
        SaleInvoice invoice = saleInvoiceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Sale invoice not found with ID: " + id));

        invoice.setInvoiceDate(dto.getInvoiceDate() != null ? dto.getInvoiceDate() : invoice.getInvoiceDate());
        invoice.setDueDate(dto.getDueDate());
        invoice.setSoCode(dto.getSoCode());
        invoice.setCustomerName(dto.getCustomerName());
        invoice.setCustomerPhone(dto.getCustomerPhone());
        invoice.setCustomerAddress(dto.getCustomerAddress());
        invoice.setSalesperson(dto.getSalesperson());
        invoice.setPaymentTerm(dto.getPaymentTerm());
        invoice.setOutlet(dto.getOutlet());
        invoice.setLocation(dto.getLocation());
        invoice.setTemplateName(dto.getTemplateName());
        invoice.setNote(dto.getNote());
        invoice.setBillingName(dto.getBillingName());
        invoice.setBillingPhone(dto.getBillingPhone());
        invoice.setBillingEmail(dto.getBillingEmail());
        invoice.setBillingAddress(dto.getBillingAddress());
        invoice.setBillingCity(dto.getBillingCity());
        invoice.setBillingTaxNo(dto.getBillingTaxNo());
        invoice.setShippingRecipient(dto.getShippingRecipient());
        invoice.setShippingPhone(dto.getShippingPhone());
        invoice.setShippingAddress(dto.getShippingAddress());
        invoice.setShippingMethod(dto.getShippingMethod());
        invoice.setTrackingNo(dto.getTrackingNo());

        if (dto.getStatus() != null) {
            invoice.setStatus(dto.getStatus());
        }

        SaleInvoice saved = saleInvoiceRepository.save(invoice);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void deleteInvoice(Long id) {
        if (!saleInvoiceRepository.existsById(id)) {
            throw new NoSuchElementException("Sale invoice not found with ID: " + id);
        }
        saleInvoiceRepository.deleteById(id);
    }

    @Override
    @Transactional
    public SaleInvoiceDto recordPayment(Long id, SaleInvoicePaymentDto paymentDto) {
        SaleInvoice invoice = saleInvoiceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Sale invoice not found with ID: " + id));

        BigDecimal exchangeRate = invoice.getExchangeRate() != null ? invoice.getExchangeRate() : DEFAULT_EXCHANGE_RATE;
        BigDecimal amount = paymentDto.getAmountDollar() != null ? paymentDto.getAmountDollar() : BigDecimal.ZERO;

        SaleInvoicePayment payment = SaleInvoicePayment.builder()
                .saleInvoice(invoice)
                .paymentDate(paymentDto.getPaymentDate() != null ? paymentDto.getPaymentDate() : LocalDateTime.now())
                .amountDollar(amount)
                .amountKhmer(paymentDto.getAmountKhmer() != null ? paymentDto.getAmountKhmer() : amount.multiply(exchangeRate))
                .paymentType(paymentDto.getPaymentType() != null ? paymentDto.getPaymentType() : "CASH")
                .reference(paymentDto.getReference())
                .note(paymentDto.getNote())
                .receivedBy(paymentDto.getReceivedBy())
                .build();

        invoice.getPayments().add(payment);

        BigDecimal currentPaid = invoice.getPaidAmount() != null ? invoice.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal newPaid = currentPaid.add(amount);
        invoice.setPaidAmount(newPaid);

        BigDecimal newBalance = invoice.getGrandTotal().subtract(newPaid);
        if (newBalance.compareTo(BigDecimal.ZERO) <= 0) {
            invoice.setBalance(BigDecimal.ZERO);
            invoice.setStatus("PAID");
        } else {
            invoice.setBalance(newBalance);
            invoice.setStatus("PARTIAL");
        }

        SaleInvoice saved = saleInvoiceRepository.save(invoice);
        return toDto(saved);
    }

    @Override
    public String generateNextInvoiceCode() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        long count = saleInvoiceRepository.count() + 1;
        String candidate = String.format("INV-%s-%04d", datePrefix, count);
        while (saleInvoiceRepository.existsByInvoiceCode(candidate)) {
            count++;
            candidate = String.format("INV-%s-%04d", datePrefix, count);
        }
        return candidate;
    }

    @Override
    @Transactional(readOnly = true)
    public SaleInvoiceStatsDto getStats() {
        List<SaleInvoice> all = saleInvoiceRepository.findAll();

        long total = all.size();
        long paid = all.stream().filter(i -> "PAID".equalsIgnoreCase(i.getStatus())).count();
        long unpaid = all.stream().filter(i -> "UNPAID".equalsIgnoreCase(i.getStatus()) || "CREDIT".equalsIgnoreCase(i.getStatus())).count();
        long partial = all.stream().filter(i -> "PARTIAL".equalsIgnoreCase(i.getStatus())).count();

        BigDecimal totalAmount = all.stream()
                .map(i -> i.getGrandTotal() != null ? i.getGrandTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPaid = all.stream()
                .map(i -> i.getPaidAmount() != null ? i.getPaidAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalBalance = all.stream()
                .map(i -> i.getBalance() != null ? i.getBalance() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDate today = LocalDate.now();
        BigDecimal todaySales = all.stream()
                .filter(i -> today.equals(i.getInvoiceDate()))
                .map(i -> i.getGrandTotal() != null ? i.getGrandTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return SaleInvoiceStatsDto.builder()
                .totalInvoices(total)
                .paidInvoices(paid)
                .unpaidInvoices(unpaid)
                .partialInvoices(partial)
                .totalAmount(totalAmount)
                .totalPaid(totalPaid)
                .totalBalance(totalBalance)
                .todaySales(todaySales)
                .build();
    }

    private SaleInvoiceDto toDto(SaleInvoice entity) {
        if (entity == null) return null;

        List<SaleInvoiceItemDto> lineDtos = entity.getLines() != null
                ? entity.getLines().stream().map(l -> SaleInvoiceItemDto.builder()
                        .id(l.getId())
                        .saleInvoiceId(entity.getId())
                        .productId(l.getProductId())
                        .productCode(l.getProductCode())
                        .description(l.getDescription())
                        .qty(l.getQty())
                        .unitPrice(l.getUnitPrice())
                        .discount(l.getDiscount())
                        .uom(l.getUom())
                        .totalPrice(l.getTotalPrice())
                        .build()).collect(Collectors.toList())
                : new ArrayList<>();

        List<SaleInvoicePaymentDto> paymentDtos = entity.getPayments() != null
                ? entity.getPayments().stream().map(p -> SaleInvoicePaymentDto.builder()
                        .id(p.getId())
                        .saleInvoiceId(entity.getId())
                        .paymentDate(p.getPaymentDate())
                        .amountDollar(p.getAmountDollar())
                        .amountKhmer(p.getAmountKhmer())
                        .paymentType(p.getPaymentType())
                        .reference(p.getReference())
                        .note(p.getNote())
                        .receivedBy(p.getReceivedBy())
                        .createdAt(p.getCreatedAt())
                        .build()).collect(Collectors.toList())
                : new ArrayList<>();

        return SaleInvoiceDto.builder()
                .id(entity.getId())
                .invoiceCode(entity.getInvoiceCode())
                .invoiceDate(entity.getInvoiceDate())
                .dueDate(entity.getDueDate())
                .soCode(entity.getSoCode())
                .customerId(entity.getCustomerId())
                .customerName(entity.getCustomerName())
                .customerPhone(entity.getCustomerPhone())
                .customerAddress(entity.getCustomerAddress())
                .salesperson(entity.getSalesperson())
                .paymentTerm(entity.getPaymentTerm())
                .outlet(entity.getOutlet())
                .location(entity.getLocation())
                .templateName(entity.getTemplateName())
                .status(entity.getStatus())
                .subTotal(entity.getSubTotal())
                .discountPercent(entity.getDiscountPercent())
                .discountAmount(entity.getDiscountAmount())
                .taxAmount(entity.getTaxAmount())
                .taxPercent(entity.getTaxPercent())
                .markupAmount(entity.getMarkupAmount())
                .grandTotal(entity.getGrandTotal())
                .paidAmount(entity.getPaidAmount())
                .balance(entity.getBalance())
                .exchangeRate(entity.getExchangeRate())
                .grandTotalKhmer(entity.getGrandTotalKhmer())
                .barcode(entity.getBarcode())
                .username(entity.getUsername())
                .note(entity.getNote())
                .paymentType(entity.getPaymentType())
                .billingName(entity.getBillingName())
                .billingPhone(entity.getBillingPhone())
                .billingEmail(entity.getBillingEmail())
                .billingAddress(entity.getBillingAddress())
                .billingCity(entity.getBillingCity())
                .billingTaxNo(entity.getBillingTaxNo())
                .shippingRecipient(entity.getShippingRecipient())
                .shippingPhone(entity.getShippingPhone())
                .shippingAddress(entity.getShippingAddress())
                .shippingMethod(entity.getShippingMethod())
                .trackingNo(entity.getTrackingNo())
                .lines(lineDtos)
                .payments(paymentDtos)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}