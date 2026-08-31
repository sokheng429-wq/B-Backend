package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.SupplierDto;
import com.bgroceries.backend.entity.Stocks.Supplier;
import com.bgroceries.backend.exception.ConflictException;
import com.bgroceries.backend.exception.NotFoundException;
import com.bgroceries.backend.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD for the Supplier resource (admin Stocks → Suppliers). A blank
 * {@code code} is auto-generated as SP-0001, SP-0002… (next free sequence);
 * a provided code must stay unique — duplicates raise ConflictException
 * (409). Blank/absent strings are stored as null so the catalog stays clean.
 */
@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;

    @Transactional(readOnly = true)
    public List<SupplierDto> getAllSuppliers() {
        return supplierRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public SupplierDto getSupplierById(Long id) {
        return toDto(findSupplier(id));
    }

    @Transactional
    public SupplierDto createSupplier(SupplierDto dto) {
        String code = normalize(dto.getCode());
        if (code == null) {
            code = generateNextCode();
        } else if (supplierRepository.findByCode(code).isPresent()) {
            throw new ConflictException("Supplier code already exists: " + code);
        }
        if (supplierRepository.existsByNameIgnoreCase(dto.getName().trim())) {
            throw new ConflictException("Supplier already exists: " + dto.getName().trim());
        }
        Supplier supplier = Supplier.builder()
                .code(code)
                .name(dto.getName().trim())
                .nameKh(normalize(dto.getNameKh()))
                .supplierGroup(normalize(dto.getSupplierGroup()))
                .taxNumber(normalize(dto.getTaxNumber()))
                .paymentTerm(normalize(dto.getPaymentTerm()))
                .poTemplateName(normalize(dto.getPoTemplateName()))
                .shipmentMethod(normalize(dto.getShipmentMethod()))
                .purchasePerson(normalize(dto.getPurchasePerson()))
                .termCondition(normalize(dto.getTermCondition()))
                .billTemplateName(normalize(dto.getBillTemplateName()))
                .currentBalance(dto.getCurrentBalance())
                .debitDepositPaymentTerm(normalize(dto.getDebitDepositPaymentTerm()))
                .contactFirstName(normalize(dto.getContactFirstName()))
                .contactLastName(normalize(dto.getContactLastName()))
                .contactGender(normalize(dto.getContactGender()))
                .contactDob(dto.getContactDob())
                .contactPhone(normalize(dto.getContactPhone()))
                .contactMobile(normalize(dto.getContactMobile()))
                .contactEmail(normalize(dto.getContactEmail()))
                .contactWebsite(normalize(dto.getContactWebsite()))
                .addressDescription(normalize(dto.getAddressDescription()))
                .addressNameKh(normalize(dto.getAddressNameKh()))
                .addressLine1(normalize(dto.getAddressLine1()))
                .addressLine2(normalize(dto.getAddressLine2()))
                .addressCity(normalize(dto.getAddressCity()))
                .addressState(normalize(dto.getAddressState()))
                .addressCountry(normalize(dto.getAddressCountry()))
                .addressPhone(normalize(dto.getAddressPhone()))
                .addressPhoneExt(normalize(dto.getAddressPhoneExt()))
                .addressFax(normalize(dto.getAddressFax()))
                .addressFaxExt(normalize(dto.getAddressFaxExt()))
                .addressEmail(normalize(dto.getAddressEmail()))
                .addressWebsite(normalize(dto.getAddressWebsite()))
                .active(dto.getActive() == null || dto.getActive())
                .build();
        return toDto(supplierRepository.save(supplier));
    }

    @Transactional
    public SupplierDto updateSupplier(Long id, SupplierDto dto) {
        Supplier supplier = findSupplier(id);
        String code = normalize(dto.getCode());
        if (code != null && supplierRepository.existsByCodeAndIdNot(code, id)) {
            throw new ConflictException("Supplier code already exists: " + code);
        }
        if (supplierRepository.existsByNameIgnoreCaseAndIdNot(dto.getName().trim(), id)) {
            throw new ConflictException("Supplier already exists: " + dto.getName().trim());
        }
        // Keep the existing generated code when the client sends it back blank.
        if (code != null) supplier.setCode(code);
        supplier.setName(dto.getName().trim());
        supplier.setNameKh(normalize(dto.getNameKh()));
        supplier.setSupplierGroup(normalize(dto.getSupplierGroup()));
        supplier.setTaxNumber(normalize(dto.getTaxNumber()));
        supplier.setPaymentTerm(normalize(dto.getPaymentTerm()));
        supplier.setPoTemplateName(normalize(dto.getPoTemplateName()));
        supplier.setShipmentMethod(normalize(dto.getShipmentMethod()));
        supplier.setPurchasePerson(normalize(dto.getPurchasePerson()));
        supplier.setTermCondition(normalize(dto.getTermCondition()));
        supplier.setBillTemplateName(normalize(dto.getBillTemplateName()));
        supplier.setCurrentBalance(dto.getCurrentBalance());
        supplier.setDebitDepositPaymentTerm(normalize(dto.getDebitDepositPaymentTerm()));
        supplier.setContactFirstName(normalize(dto.getContactFirstName()));
        supplier.setContactLastName(normalize(dto.getContactLastName()));
        supplier.setContactGender(normalize(dto.getContactGender()));
        supplier.setContactDob(dto.getContactDob());
        supplier.setContactPhone(normalize(dto.getContactPhone()));
        supplier.setContactMobile(normalize(dto.getContactMobile()));
        supplier.setContactEmail(normalize(dto.getContactEmail()));
        supplier.setContactWebsite(normalize(dto.getContactWebsite()));
        supplier.setAddressDescription(normalize(dto.getAddressDescription()));
        supplier.setAddressNameKh(normalize(dto.getAddressNameKh()));
        supplier.setAddressLine1(normalize(dto.getAddressLine1()));
        supplier.setAddressLine2(normalize(dto.getAddressLine2()));
        supplier.setAddressCity(normalize(dto.getAddressCity()));
        supplier.setAddressState(normalize(dto.getAddressState()));
        supplier.setAddressCountry(normalize(dto.getAddressCountry()));
        supplier.setAddressPhone(normalize(dto.getAddressPhone()));
        supplier.setAddressPhoneExt(normalize(dto.getAddressPhoneExt()));
        supplier.setAddressFax(normalize(dto.getAddressFax()));
        supplier.setAddressFaxExt(normalize(dto.getAddressFaxExt()));
        supplier.setAddressEmail(normalize(dto.getAddressEmail()));
        supplier.setAddressWebsite(normalize(dto.getAddressWebsite()));
        if (dto.getActive() != null) supplier.setActive(dto.getActive());
        return toDto(supplierRepository.save(supplier));
    }

    @Transactional
    public void deleteSupplier(Long id) {
        supplierRepository.delete(findSupplier(id));
    }

    /** SP-#### — one past the highest existing sequence, zero-padded to 4 digits. */
    private String generateNextCode() {
        long next = supplierRepository.maxSequenceNumber() + 1;
        return String.format("SP-%04d", next);
    }

    private Supplier findSupplier(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Supplier not found"));
    }

    /** Trim, and turn blanks into null so optional columns stay clean. */
    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private SupplierDto toDto(Supplier s) {
        return SupplierDto.builder()
                .id(s.getId())
                .code(s.getCode())
                .name(s.getName())
                .nameKh(s.getNameKh())
                .supplierGroup(s.getSupplierGroup())
                .taxNumber(s.getTaxNumber())
                .paymentTerm(s.getPaymentTerm())
                .poTemplateName(s.getPoTemplateName())
                .shipmentMethod(s.getShipmentMethod())
                .purchasePerson(s.getPurchasePerson())
                .termCondition(s.getTermCondition())
                .billTemplateName(s.getBillTemplateName())
                .currentBalance(s.getCurrentBalance())
                .debitDepositPaymentTerm(s.getDebitDepositPaymentTerm())
                .contactFirstName(s.getContactFirstName())
                .contactLastName(s.getContactLastName())
                .contactGender(s.getContactGender())
                .contactDob(s.getContactDob())
                .contactPhone(s.getContactPhone())
                .contactMobile(s.getContactMobile())
                .contactEmail(s.getContactEmail())
                .contactWebsite(s.getContactWebsite())
                .addressDescription(s.getAddressDescription())
                .addressNameKh(s.getAddressNameKh())
                .addressLine1(s.getAddressLine1())
                .addressLine2(s.getAddressLine2())
                .addressCity(s.getAddressCity())
                .addressState(s.getAddressState())
                .addressCountry(s.getAddressCountry())
                .addressPhone(s.getAddressPhone())
                .addressPhoneExt(s.getAddressPhoneExt())
                .addressFax(s.getAddressFax())
                .addressFaxExt(s.getAddressFaxExt())
                .addressEmail(s.getAddressEmail())
                .addressWebsite(s.getAddressWebsite())
                .active(s.getActive())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
