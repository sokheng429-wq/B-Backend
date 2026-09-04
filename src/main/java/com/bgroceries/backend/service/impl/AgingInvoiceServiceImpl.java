package com.bgroceries.backend.service.impl;

import com.bgroceries.backend.dto.AgingInvoiceDto;
import com.bgroceries.backend.entity.Sale.AgingInvoice;
import com.bgroceries.backend.entity.Sale.Customer;
import com.bgroceries.backend.entity.Sale.SaleInvoice;
import com.bgroceries.backend.repository.AgingInvoiceRepository;
import com.bgroceries.backend.repository.CustomerRepository;
import com.bgroceries.backend.repository.SaleInvoiceRepository;
import com.bgroceries.backend.service.AgingInvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgingInvoiceServiceImpl implements AgingInvoiceService {

    private final AgingInvoiceRepository agingInvoiceRepository;
    private final SaleInvoiceRepository saleInvoiceRepository;
    private final CustomerRepository customerRepository;

    @Override
    public List<AgingInvoiceDto> getAllAgingInvoices(
            String search,
            String searchBy,
            String agingType,
            String salesperson,
            String customer,
            String customerGroup
    ) {
        LocalDate today = LocalDate.now();

        // 1. Build a lookup of customers for Customer Group resolution
        Map<String, String> customerGroupMap = new HashMap<>();
        try {
            List<Customer> allCustomers = customerRepository.findAll();
            for (Customer c : allCustomers) {
                if (c.getCustomerName() != null && c.getCustomerGroup() != null) {
                    customerGroupMap.put(c.getCustomerName().trim().toLowerCase(), c.getCustomerGroup().trim());
                }
            }
        } catch (Exception e) {
            log.warn("Could not load customer groups: {}", e.getMessage());
        }

        List<AgingInvoiceDto> allItems = new ArrayList<>();
        Set<String> seenCodes = new HashSet<>();

        // 2. Load live SaleInvoices from actual database
        try {
            List<SaleInvoice> saleInvoices = saleInvoiceRepository.findAll();
            for (SaleInvoice inv : saleInvoices) {
                if (inv.getInvoiceCode() == null) continue;
                String code = inv.getInvoiceCode().trim();
                seenCodes.add(code);

                LocalDate invDate = inv.getInvoiceDate() != null ? inv.getInvoiceDate() : (inv.getCreatedAt() != null ? inv.getCreatedAt().toLocalDate() : today);
                LocalDate dueDate = inv.getDueDate() != null ? inv.getDueDate() : invDate;

                int daysOverdue = (int) ChronoUnit.DAYS.between(dueDate, today);
                if (daysOverdue < 0) daysOverdue = 0;

                String type = calculateAgingType(dueDate, today);

                String custName = inv.getCustomerName() != null ? inv.getCustomerName() : "";
                String contact = inv.getBillingName() != null && !inv.getBillingName().isBlank()
                        ? inv.getBillingName()
                        : (inv.getShippingRecipient() != null && !inv.getShippingRecipient().isBlank() ? inv.getShippingRecipient() : custName);

                String phone = inv.getCustomerPhone() != null && !inv.getCustomerPhone().isBlank()
                        ? inv.getCustomerPhone()
                        : (inv.getBillingPhone() != null ? inv.getBillingPhone() : "");

                String resolvedGroup = customerGroupMap.getOrDefault(custName.trim().toLowerCase(), "");

                AgingInvoiceDto dto = AgingInvoiceDto.builder()
                        .id(inv.getId())
                        .code(code)
                        .date(invDate)
                        .dueDate(dueDate)
                        .customer(custName)
                        .contactName(contact)
                        .phone(phone)
                        .status(inv.getStatus() != null ? inv.getStatus() : "UNPAID")
                        .grandTotal(inv.getGrandTotal() != null ? inv.getGrandTotal() : BigDecimal.ZERO)
                        .balance(inv.getBalance() != null ? inv.getBalance() : BigDecimal.ZERO)
                        .salesperson(inv.getSalesperson() != null ? inv.getSalesperson() : "")
                        .customerGroup(resolvedGroup)
                        .daysOverdue(daysOverdue)
                        .agingType(type)
                        .build();

                allItems.add(dto);
            }
        } catch (Exception e) {
            log.error("Error reading live sale invoices for aging: {}", e.getMessage());
        }

        // 3. Also load any standalone AgingInvoice entries
        try {
            List<AgingInvoice> standalone = agingInvoiceRepository.findAll();
            for (AgingInvoice ag : standalone) {
                if (ag.getCode() != null && !seenCodes.contains(ag.getCode().trim())) {
                    LocalDate dueDate = ag.getDueDate() != null ? ag.getDueDate() : ag.getDate();
                    int daysOverdue = (int) ChronoUnit.DAYS.between(dueDate, today);
                    if (daysOverdue < 0) daysOverdue = 0;
                    String type = calculateAgingType(dueDate, today);

                    AgingInvoiceDto dto = AgingInvoiceDto.builder()
                            .id(ag.getId())
                            .code(ag.getCode())
                            .date(ag.getDate())
                            .dueDate(dueDate)
                            .customer(ag.getCustomer())
                            .contactName(ag.getContactName() != null ? ag.getContactName() : "")
                            .phone(ag.getPhone() != null ? ag.getPhone() : "")
                            .status(ag.getStatus() != null ? ag.getStatus() : "UNPAID")
                            .grandTotal(ag.getGrandTotal() != null ? ag.getGrandTotal() : BigDecimal.ZERO)
                            .balance(ag.getBalance() != null ? ag.getBalance() : BigDecimal.ZERO)
                            .salesperson(ag.getSalesperson() != null ? ag.getSalesperson() : "")
                            .customerGroup(ag.getCustomerGroup() != null ? ag.getCustomerGroup() : "")
                            .daysOverdue(daysOverdue)
                            .agingType(type)
                            .build();

                    allItems.add(dto);
                }
            }
        } catch (Exception e) {
            log.warn("Error reading standalone aging invoices: {}", e.getMessage());
        }

        // 4. Apply filters
        String sTerm = search != null ? search.trim().toLowerCase() : "";
        String sBy = searchBy != null ? searchBy.trim().toLowerCase() : "any";
        String aType = agingType != null ? agingType.trim().toUpperCase() : "ALL";
        String sPerson = salesperson != null ? salesperson.trim().toLowerCase() : "";
        String sCust = customer != null ? customer.trim().toLowerCase() : "";
        String sGrp = customerGroup != null ? customerGroup.trim().toLowerCase() : "";

        return allItems.stream()
                .filter(item -> {
                    // Aging Type Filter
                    if (!aType.equals("ALL") && !aType.isBlank()) {
                        if (!aType.equalsIgnoreCase(item.getAgingType())) {
                            return false;
                        }
                    }

                    // Salesperson Filter
                    if (!sPerson.isBlank() && !sPerson.equals("all")) {
                        if (item.getSalesperson() == null || !item.getSalesperson().toLowerCase().contains(sPerson)) {
                            return false;
                        }
                    }

                    // Customer Filter
                    if (!sCust.isBlank() && !sCust.equals("all")) {
                        if (item.getCustomer() == null || !item.getCustomer().toLowerCase().contains(sCust)) {
                            return false;
                        }
                    }

                    // Customer Group Filter
                    if (!sGrp.isBlank() && !sGrp.equals("all")) {
                        if (item.getCustomerGroup() == null || !item.getCustomerGroup().toLowerCase().contains(sGrp)) {
                            return false;
                        }
                    }

                    // Search Term Filter
                    if (sTerm.isBlank()) return true;

                    switch (sBy) {
                        case "code":
                            return item.getCode() != null && item.getCode().toLowerCase().contains(sTerm);
                        case "date":
                            return (item.getDate() != null && item.getDate().toString().contains(sTerm))
                                    || (item.getDueDate() != null && item.getDueDate().toString().contains(sTerm));
                        case "phone":
                            return item.getPhone() != null && item.getPhone().toLowerCase().contains(sTerm);
                        case "customer":
                            return item.getCustomer() != null && item.getCustomer().toLowerCase().contains(sTerm);
                        case "contactname":
                        case "contact_name":
                        case "contact":
                            return item.getContactName() != null && item.getContactName().toLowerCase().contains(sTerm);
                        case "any":
                        default:
                            return (item.getCode() != null && item.getCode().toLowerCase().contains(sTerm))
                                    || (item.getCustomer() != null && item.getCustomer().toLowerCase().contains(sTerm))
                                    || (item.getContactName() != null && item.getContactName().toLowerCase().contains(sTerm))
                                    || (item.getPhone() != null && item.getPhone().toLowerCase().contains(sTerm))
                                    || (item.getDate() != null && item.getDate().toString().contains(sTerm))
                                    || (item.getStatus() != null && item.getStatus().toLowerCase().contains(sTerm));
                    }
                })
                .sorted((a, b) -> {
                    // Sort descending by days overdue (most urgent overdue first)
                    int cmp = Integer.compare(b.getDaysOverdue() != null ? b.getDaysOverdue() : 0, a.getDaysOverdue() != null ? a.getDaysOverdue() : 0);
                    if (cmp != 0) return cmp;
                    return b.getDate().compareTo(a.getDate());
                })
                .collect(Collectors.toList());
    }

    @Override
    public AgingInvoiceDto getAgingInvoiceById(Long id) {
        List<AgingInvoiceDto> all = getAllAgingInvoices(null, null, null, null, null, null);
        return all.stream()
                .filter(i -> Objects.equals(i.getId(), id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Map<String, Object> getAgingSummary() {
        List<AgingInvoiceDto> all = getAllAgingInvoices(null, null, null, null, null, null);

        BigDecimal totalReceivable = BigDecimal.ZERO;
        BigDecimal currentTotal = BigDecimal.ZERO;
        BigDecimal total1_30 = BigDecimal.ZERO;
        BigDecimal total31_60 = BigDecimal.ZERO;
        BigDecimal total61_90 = BigDecimal.ZERO;
        BigDecimal total91_120 = BigDecimal.ZERO;
        BigDecimal totalOver120 = BigDecimal.ZERO;

        int countCurrent = 0;
        int count1_30 = 0;
        int count31_60 = 0;
        int count61_90 = 0;
        int count91_120 = 0;
        int countOver120 = 0;

        for (AgingInvoiceDto inv : all) {
            BigDecimal bal = inv.getBalance() != null ? inv.getBalance() : BigDecimal.ZERO;
            totalReceivable = totalReceivable.add(bal);

            String type = inv.getAgingType() != null ? inv.getAgingType() : "CURRENT";
            switch (type) {
                case "CURRENT":
                    currentTotal = currentTotal.add(bal);
                    countCurrent++;
                    break;
                case "1_30":
                    total1_30 = total1_30.add(bal);
                    count1_30++;
                    break;
                case "31_60":
                    total31_60 = total31_60.add(bal);
                    count31_60++;
                    break;
                case "61_90":
                    total61_90 = total61_90.add(bal);
                    count61_90++;
                    break;
                case "91_120":
                    total91_120 = total91_120.add(bal);
                    count91_120++;
                    break;
                case "OVER_120":
                    totalOver120 = totalOver120.add(bal);
                    countOver120++;
                    break;
            }
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalInvoices", all.size());
        summary.put("totalReceivable", totalReceivable);
        summary.put("currentTotal", currentTotal);
        summary.put("countCurrent", countCurrent);
        summary.put("total1_30", total1_30);
        summary.put("count1_30", count1_30);
        summary.put("total31_60", total31_60);
        summary.put("count31_60", count31_60);
        summary.put("total61_90", total61_90);
        summary.put("count61_90", count61_90);
        summary.put("total91_120", total91_120);
        summary.put("count91_120", count91_120);
        summary.put("totalOver120", totalOver120);
        summary.put("countOver120", countOver120);

        return summary;
    }

    private String calculateAgingType(LocalDate dueDate, LocalDate today) {
        if (dueDate == null || !dueDate.isBefore(today)) {
            return "CURRENT"; // Due date is today or in future
        }
        long days = ChronoUnit.DAYS.between(dueDate, today);
        if (days <= 0) return "CURRENT";
        if (days <= 30) return "1_30";
        if (days <= 60) return "31_60";
        if (days <= 90) return "61_90";
        if (days <= 120) return "91_120";
        return "OVER_120";
    }
}
