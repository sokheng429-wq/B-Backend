package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.CustomerDto;
import com.bgroceries.backend.entity.Sale.Customer;
import com.bgroceries.backend.exception.ConflictException;
import com.bgroceries.backend.exception.NotFoundException;
import com.bgroceries.backend.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public List<CustomerDto> getAllCustomers() {
        return customerRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CustomerDto getCustomerById(Long id) {
        return toDto(findCustomer(id));
    }

    @Transactional
    public CustomerDto createCustomer(CustomerDto dto) {
        if (customerRepository.existsByCustomerNameIgnoreCase(dto.getCustomerName().trim())) {
            throw new ConflictException("Customer already exists: " + dto.getCustomerName().trim());
        }
        String code = normalize(dto.getCode());
        if (code == null) {
            code = generateNextCode();
        } else if (customerRepository.findByCode(code).isPresent()) {
            throw new ConflictException("Customer code already exists: " + code);
        }

        Customer customer = Customer.builder()
                .code(code)
                .customerName(dto.getCustomerName().trim())
                .secondLanguage(normalize(dto.getSecondLanguage()))
                .customerGroup(normalize(dto.getCustomerGroup()))
                .saleEmployee(normalize(dto.getSaleEmployee()))
                .taxNo(normalize(dto.getTaxNo()))
                .paymentTerm(normalize(dto.getPaymentTerm()))
                .termsAndCondition(normalize(dto.getTermsAndCondition()))
                .priceBook(normalize(dto.getPriceBook()))
                .quoteTemplate(normalize(dto.getQuoteTemplate()))
                .soTemplate(normalize(dto.getSoTemplate()))
                .invoiceTemplate(normalize(dto.getInvoiceTemplate()))
                .doTemplate(normalize(dto.getDoTemplate()))
                .allowCredit(dto.getAllowCredit() != null && dto.getAllowCredit())
                .creditLimit(dto.getCreditLimit())
                .currentBalance(dto.getCurrentBalance())
                .creditDeposit(dto.getCreditDeposit())
                .balance(dto.getBalance())
                .contactFirstName(normalize(dto.getContactFirstName()))
                .contactLastName(normalize(dto.getContactLastName()))
                .contactGender(normalize(dto.getContactGender()))
                .contactDob(dto.getContactDob())
                .contactPhone(normalize(dto.getContactPhone()))
                .contactMobile(normalize(dto.getContactMobile()))
                .contactEmail(normalize(dto.getContactEmail()))
                .contactWebsite(normalize(dto.getContactWebsite()))
                .addressDescription(normalize(dto.getAddressDescription()))
                .addressSecondLanguage(normalize(dto.getAddressSecondLanguage()))
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
        return toDto(customerRepository.save(customer));
    }

    @Transactional
    public CustomerDto updateCustomer(Long id, CustomerDto dto) {
        Customer customer = findCustomer(id);
        String code = normalize(dto.getCode());
        if (code != null && customerRepository.existsByCodeAndIdNot(code, id)) {
            throw new ConflictException("Customer code already exists: " + code);
        }
        if (customerRepository.existsByCustomerNameIgnoreCaseAndIdNot(dto.getCustomerName().trim(), id)) {
            throw new ConflictException("Customer already exists: " + dto.getCustomerName().trim());
        }
        if (code != null) customer.setCode(code);
        customer.setCustomerName(dto.getCustomerName().trim());
        customer.setSecondLanguage(normalize(dto.getSecondLanguage()));
        customer.setCustomerGroup(normalize(dto.getCustomerGroup()));
        customer.setSaleEmployee(normalize(dto.getSaleEmployee()));
        customer.setTaxNo(normalize(dto.getTaxNo()));
        customer.setPaymentTerm(normalize(dto.getPaymentTerm()));
        customer.setTermsAndCondition(normalize(dto.getTermsAndCondition()));
        customer.setPriceBook(normalize(dto.getPriceBook()));
        customer.setQuoteTemplate(normalize(dto.getQuoteTemplate()));
        customer.setSoTemplate(normalize(dto.getSoTemplate()));
        customer.setInvoiceTemplate(normalize(dto.getInvoiceTemplate()));
        customer.setDoTemplate(normalize(dto.getDoTemplate()));
        customer.setAllowCredit(dto.getAllowCredit() != null && dto.getAllowCredit());
        customer.setCreditLimit(dto.getCreditLimit());
        customer.setCurrentBalance(dto.getCurrentBalance());
        customer.setCreditDeposit(dto.getCreditDeposit());
        customer.setBalance(dto.getBalance());
        customer.setContactFirstName(normalize(dto.getContactFirstName()));
        customer.setContactLastName(normalize(dto.getContactLastName()));
        customer.setContactGender(normalize(dto.getContactGender()));
        customer.setContactDob(dto.getContactDob());
        customer.setContactPhone(normalize(dto.getContactPhone()));
        customer.setContactMobile(normalize(dto.getContactMobile()));
        customer.setContactEmail(normalize(dto.getContactEmail()));
        customer.setContactWebsite(normalize(dto.getContactWebsite()));
        customer.setAddressDescription(normalize(dto.getAddressDescription()));
        customer.setAddressSecondLanguage(normalize(dto.getAddressSecondLanguage()));
        customer.setAddressLine1(normalize(dto.getAddressLine1()));
        customer.setAddressLine2(normalize(dto.getAddressLine2()));
        customer.setAddressCity(normalize(dto.getAddressCity()));
        customer.setAddressState(normalize(dto.getAddressState()));
        customer.setAddressCountry(normalize(dto.getAddressCountry()));
        customer.setAddressPhone(normalize(dto.getAddressPhone()));
        customer.setAddressPhoneExt(normalize(dto.getAddressPhoneExt()));
        customer.setAddressFax(normalize(dto.getAddressFax()));
        customer.setAddressFaxExt(normalize(dto.getAddressFaxExt()));
        customer.setAddressEmail(normalize(dto.getAddressEmail()));
        customer.setAddressWebsite(normalize(dto.getAddressWebsite()));
        if (dto.getActive() != null) customer.setActive(dto.getActive());
        return toDto(customerRepository.save(customer));
    }

    @Transactional
    public void deleteCustomer(Long id) {
        customerRepository.delete(findCustomer(id));
    }

    private String generateNextCode() {
        long next = customerRepository.maxSequenceNumber() + 1;
        return String.format("CU-%04d", next);
    }

    private Customer findCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found"));
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private CustomerDto toDto(Customer c) {
        return CustomerDto.builder()
                .id(c.getId())
                .code(c.getCode())
                .customerName(c.getCustomerName())
                .secondLanguage(c.getSecondLanguage())
                .customerGroup(c.getCustomerGroup())
                .saleEmployee(c.getSaleEmployee())
                .taxNo(c.getTaxNo())
                .paymentTerm(c.getPaymentTerm())
                .termsAndCondition(c.getTermsAndCondition())
                .priceBook(c.getPriceBook())
                .quoteTemplate(c.getQuoteTemplate())
                .soTemplate(c.getSoTemplate())
                .invoiceTemplate(c.getInvoiceTemplate())
                .doTemplate(c.getDoTemplate())
                .allowCredit(c.getAllowCredit())
                .creditLimit(c.getCreditLimit())
                .currentBalance(c.getCurrentBalance())
                .creditDeposit(c.getCreditDeposit())
                .balance(c.getBalance())
                .contactFirstName(c.getContactFirstName())
                .contactLastName(c.getContactLastName())
                .contactGender(c.getContactGender())
                .contactDob(c.getContactDob())
                .contactPhone(c.getContactPhone())
                .contactMobile(c.getContactMobile())
                .contactEmail(c.getContactEmail())
                .contactWebsite(c.getContactWebsite())
                .addressDescription(c.getAddressDescription())
                .addressSecondLanguage(c.getAddressSecondLanguage())
                .addressLine1(c.getAddressLine1())
                .addressLine2(c.getAddressLine2())
                .addressCity(c.getAddressCity())
                .addressState(c.getAddressState())
                .addressCountry(c.getAddressCountry())
                .addressPhone(c.getAddressPhone())
                .addressPhoneExt(c.getAddressPhoneExt())
                .addressFax(c.getAddressFax())
                .addressFaxExt(c.getAddressFaxExt())
                .addressEmail(c.getAddressEmail())
                .addressWebsite(c.getAddressWebsite())
                .active(c.getActive())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}