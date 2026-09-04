package com.bgroceries.backend.service.impl;

import com.bgroceries.backend.dto.CashOperationDto;
import com.bgroceries.backend.entity.Cash.CashOperation;
import com.bgroceries.backend.repository.CashOperationRepository;
import com.bgroceries.backend.service.CashOperationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashOperationServiceImpl implements CashOperationService {

    private final CashOperationRepository repository;

    @PostConstruct
    public void init() {
        seedInitialData();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashOperationDto> searchOperations(String search, String searchBy, String type, String outlet, String status, LocalDateTime fromDate, LocalDateTime toDate) {
        List<CashOperation> list = repository.findAllByOrderByTransactionDateDesc();

        if (type != null && !type.trim().isEmpty() && !type.equalsIgnoreCase("all") && !type.equalsIgnoreCase("any")) {
            String normType = type.trim().replace(" ", "_").toUpperCase();
            list = list.stream().filter(c -> c.getType() != null && c.getType().equalsIgnoreCase(normType)).collect(Collectors.toList());
        }

        if (outlet != null && !outlet.trim().isEmpty() && !outlet.equalsIgnoreCase("all") && !outlet.equalsIgnoreCase("any")) {
            String normOutlet = outlet.trim().toLowerCase();
            list = list.stream().filter(c -> c.getOutlet() != null && c.getOutlet().toLowerCase().equals(normOutlet)).collect(Collectors.toList());
        }

        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("all") && !status.equalsIgnoreCase("any")) {
            String normStatus = status.trim().replace("-", "_").replace(" ", "_").toUpperCase();
            list = list.stream().filter(c -> c.getStatus() != null && c.getStatus().equalsIgnoreCase(normStatus)).collect(Collectors.toList());
        }

        if (fromDate != null) {
            list = list.stream().filter(c -> c.getTransactionDate() != null && !c.getTransactionDate().isBefore(fromDate)).collect(Collectors.toList());
        }

        if (toDate != null) {
            list = list.stream().filter(c -> c.getTransactionDate() != null && !c.getTransactionDate().isAfter(toDate)).collect(Collectors.toList());
        }

        if (search != null && !search.trim().isEmpty()) {
            String q = search.trim().toLowerCase();
            String mode = (searchBy != null && !searchBy.trim().isEmpty()) ? searchBy.trim() : "Any";

            list = list.stream().filter(c -> {
                if (mode.equalsIgnoreCase("Code")) {
                    return c.getCode() != null && c.getCode().toLowerCase().contains(q);
                } else if (mode.equalsIgnoreCase("Customer")) {
                    return ("CUSTOMER".equalsIgnoreCase(c.getPartyType()) || c.getPartyType() == null) &&
                           c.getPartyName() != null && c.getPartyName().toLowerCase().contains(q);
                } else if (mode.equalsIgnoreCase("Supplier")) {
                    return ("SUPPLIER".equalsIgnoreCase(c.getPartyType()) || c.getPartyType() == null) &&
                           c.getPartyName() != null && c.getPartyName().toLowerCase().contains(q);
                } else {
                    return (c.getCode() != null && c.getCode().toLowerCase().contains(q)) ||
                           (c.getPartyName() != null && c.getPartyName().toLowerCase().contains(q)) ||
                           (c.getDescription() != null && c.getDescription().toLowerCase().contains(q)) ||
                           (c.getReferenceNo() != null && c.getReferenceNo().toLowerCase().contains(q)) ||
                           (c.getUsername() != null && c.getUsername().toLowerCase().contains(q)) ||
                           (c.getCategory() != null && c.getCategory().toLowerCase().contains(q));
                }
            }).collect(Collectors.toList());
        }

        return list.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CashOperationDto getById(Long id) {
        return repository.findById(id).map(this::mapToDto).orElse(null);
    }

    @Override
    @Transactional
    public CashOperationDto voidOperation(Long id) {
        CashOperation op = repository.findById(id).orElse(null);
        if (op == null) return null;
        op.setStatus("VOIDED");
        return mapToDto(repository.save(op));
    }

    @Override
    @Transactional
    public void seedInitialData() {
        if (repository.count() > 0) return;

        List<CashOperation> seeds = new ArrayList<>();

        seeds.add(CashOperation.builder()
                .code("CIN-2026-0001")
                .transactionDate(LocalDateTime.now().minusDays(1).withHour(9).withMinute(15))
                .type("CASH_IN")
                .partyType("CUSTOMER")
                .partyName("Angkor Fresh Market (K. Sophea)")
                .amount(450.00)
                .outlet("Main Store Warehouse")
                .status("NON_VOIDED")
                .category("Store Sales Intake")
                .referenceNo("REC-98214")
                .description("Counter sales daily cash deposit from POS Terminal 1")
                .username("CashierDara")
                .build());

        seeds.add(CashOperation.builder()
                .code("COUT-2026-0002")
                .transactionDate(LocalDateTime.now().minusDays(1).withHour(11).withMinute(40))
                .type("CASH_OUT")
                .partyType("SUPPLIER")
                .partyName("Battambang Organic Rice Ltd")
                .amount(1200.00)
                .outlet("Main Store Warehouse")
                .status("NON_VOIDED")
                .category("Supplier Advance")
                .referenceNo("VCH-00431")
                .description("Advance cash deposit for premium jasmine rice shipment")
                .username("Badmin")
                .build());

        seeds.add(CashOperation.builder()
                .code("CIN-2026-0003")
                .transactionDate(LocalDateTime.now().minusDays(2).withHour(14).withMinute(20))
                .type("CASH_IN")
                .partyType("CUSTOMER")
                .partyName("Sovannaphum Mart")
                .amount(320.50)
                .outlet("Central Cold Storage")
                .status("NON_VOIDED")
                .category("AR Collection")
                .referenceNo("REC-98219")
                .description("Customer cash settlement for weekly fresh produce invoice")
                .username("CashierChann")
                .build());

        seeds.add(CashOperation.builder()
                .code("COUT-2026-0004")
                .transactionDate(LocalDateTime.now().minusDays(2).withHour(16).withMinute(05))
                .type("CASH_OUT")
                .partyType("SUPPLIER")
                .partyName("Kirirom Dairy Co.")
                .amount(85.00)
                .outlet("Express Mart BKK1")
                .status("NON_VOIDED")
                .category("Petty Cash Expense")
                .referenceNo("PET-0912")
                .description("Emergency store supplies and cooler cleaning items")
                .username("Badmin")
                .build());

        seeds.add(CashOperation.builder()
                .code("CIN-2026-0005")
                .transactionDate(LocalDateTime.now().minusDays(3).withHour(10).withMinute(30))
                .type("CASH_IN")
                .partyType("CUSTOMER")
                .partyName("Phnom Penh Grocery Hub")
                .amount(850.00)
                .outlet("Toul Kork Branch")
                .status("NON_VOIDED")
                .category("Customer Advance")
                .referenceNo("DEP-7701")
                .description("Customer downpayment for weekend catering order")
                .username("CashierDara")
                .build());

        seeds.add(CashOperation.builder()
                .code("COUT-2026-0006")
                .transactionDate(LocalDateTime.now().minusDays(3).withHour(15).withMinute(45))
                .type("CASH_OUT")
                .partyType("OTHER")
                .partyName("EDC - Electricite du Cambodge")
                .amount(340.00)
                .outlet("Chbar Ampov Depot")
                .status("NON_VOIDED")
                .category("Utility Expense")
                .referenceNo("EDC-09412")
                .description("Warehouse electricity monthly utility invoice payment")
                .username("Badmin")
                .build());

        seeds.add(CashOperation.builder()
                .code("CIN-2026-0007")
                .transactionDate(LocalDateTime.now().minusDays(4).withHour(13).withMinute(10))
                .type("CASH_IN")
                .partyType("SUPPLIER")
                .partyName("Mekong River Fisheries")
                .amount(180.00)
                .outlet("Siem Reap Hub")
                .status("NON_VOIDED")
                .category("Supplier Refund")
                .referenceNo("REF-0412")
                .description("Refund for returned defective packaging cartons")
                .username("CashierChann")
                .build());

        seeds.add(CashOperation.builder()
                .code("COUT-2026-0008")
                .transactionDate(LocalDateTime.now().minusDays(5).withHour(17).withMinute(20))
                .type("CASH_OUT")
                .partyType("SUPPLIER")
                .partyName("Angkor Express Logistics")
                .amount(210.00)
                .outlet("Main Store Warehouse")
                .status("VOIDED")
                .category("Freight Charge")
                .referenceNo("FRT-8812")
                .description("Courier freight voucher - duplicate entry voided by admin")
                .username("Badmin")
                .build());

        repository.saveAll(seeds);
        log.info("Successfully seeded {} CashOperation demo records.", seeds.size());
    }

    private CashOperationDto mapToDto(CashOperation entity) {
        if (entity == null) return null;
        return CashOperationDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .transactionDate(entity.getTransactionDate())
                .type(entity.getType())
                .partyType(entity.getPartyType())
                .partyName(entity.getPartyName())
                .amount(entity.getAmount())
                .outlet(entity.getOutlet())
                .status(entity.getStatus())
                .category(entity.getCategory())
                .referenceNo(entity.getReferenceNo())
                .description(entity.getDescription())
                .username(entity.getUsername())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
