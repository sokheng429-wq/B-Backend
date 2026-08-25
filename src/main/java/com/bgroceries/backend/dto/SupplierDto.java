package com.bgroceries.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Combined request/response DTO for the Supplier resource (admin
 * Stocks → Suppliers CRUD). Field names are part of the API contract — do not
 * rename. Only {@code name} is required; {@code code} is auto-generated as
 * SP-#### when omitted. The dropdown-backed fields (supplierGroup,
 * paymentTerm, poTemplateName, shipmentMethod, purchasePerson, termCondition,
 * billTemplateName) hold free text today — the dropdown options are supplied
 * by the frontend until dedicated master-data tables exist.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDto {

    private Long id;

    /** Optional on create — the backend generates SP-0001, SP-0002… when blank. */
    @Size(max = 50, message = "Code must be at most 50 characters")
    private String code;

    /** Supplier business/contact name. */
    @NotBlank(message = "Supplier name is required")
    @Size(max = 200, message = "Supplier name must be at most 200 characters")
    private String name;

    /** Second-language (Khmer) display text. */
    @Size(max = 200, message = "Second language must be at most 200 characters")
    private String nameKh;

    /** Supplier group this vendor belongs to (Stocks → Suppliers Group). */
    @Size(max = 200, message = "Supplier group must be at most 200 characters")
    private String supplierGroup;

    @Size(max = 100, message = "Tax number must be at most 100 characters")
    private String taxNumber;

    /** Payment term, e.g. NET-30 or COD. */
    @Size(max = 100, message = "Payment term must be at most 100 characters")
    private String paymentTerm;

    /** Default purchase-order template used when buying from this supplier. */
    @Size(max = 200, message = "PO template must be at most 200 characters")
    private String poTemplateName;

    /** Preferred shipment method, e.g. Road / Air / Sea. */
    @Size(max = 200, message = "Shipment method must be at most 200 characters")
    private String shipmentMethod;

    /** Purchasing person in charge of this supplier. */
    @Size(max = 200, message = "Purchase person must be at most 200 characters")
    private String purchasePerson;

    /** Agreed terms and conditions for purchases from this supplier. */
    @Size(max = 200, message = "Terms and condition must be at most 200 characters")
    private String termCondition;

    /** Default bill template used when recording this supplier's bills. */
    @Size(max = 200, message = "Bill template must be at most 200 characters")
    private String billTemplateName;

    /** Running balance owed to/by this supplier. */
    private java.math.BigDecimal currentBalance;

    /** Debit / deposit payment term agreed with this supplier. */
    @Size(max = 100, message = "Debit/ deposit payment term must be at most 100 characters")
    private String debitDepositPaymentTerm;

    // ---- Default contact (Contact Information section of the form) -------
    // The modal's contact sub-table persists its default row into these
    // fields. Field names are part of the API contract — do not rename.

    @Size(max = 100, message = "First name must be at most 100 characters")
    private String contactFirstName;

    @Size(max = 100, message = "Last name must be at most 100 characters")
    private String contactLastName;

    /** Male / Female / Other. */
    @Size(max = 20, message = "Gender must be at most 20 characters")
    private String contactGender;

    private java.time.LocalDate contactDob;

    @Size(max = 50, message = "Phone must be at most 50 characters")
    private String contactPhone;

    @Size(max = 50, message = "Mobile must be at most 50 characters")
    private String contactMobile;

    @Email(message = "Email must be a valid email address")
    @Size(max = 200, message = "Email must be at most 200 characters")
    private String contactEmail;

    @Size(max = 200, message = "Website must be at most 200 characters")
    private String contactWebsite;

    // ---- Default address (Location Information section of the form) ------

    @Size(max = 200, message = "Address description must be at most 200 characters")
    private String addressDescription;

    /** Second-language (Khmer) display text for the address. */
    @Size(max = 200, message = "Second language must be at most 200 characters")
    private String addressNameKh;

    @Size(max = 300, message = "Address must be at most 300 characters")
    private String addressLine1;

    @Size(max = 300, message = "Address 2 must be at most 300 characters")
    private String addressLine2;

    @Size(max = 100, message = "City must be at most 100 characters")
    private String addressCity;

    /** State / Province. */
    @Size(max = 100, message = "State must be at most 100 characters")
    private String addressState;

    @Size(max = 100, message = "Country must be at most 100 characters")
    private String addressCountry;

    @Size(max = 50, message = "Phone must be at most 50 characters")
    private String addressPhone;

    @Size(max = 20, message = "Phone ext must be at most 20 characters")
    private String addressPhoneExt;

    @Size(max = 50, message = "Fax must be at most 50 characters")
    private String addressFax;

    @Size(max = 20, message = "Fax ext must be at most 20 characters")
    private String addressFaxExt;

    @Email(message = "Email must be a valid email address")
    @Size(max = 200, message = "Email must be at most 200 characters")
    private String addressEmail;

    @Size(max = 200, message = "Website must be at most 200 characters")
    private String addressWebsite;

    private Boolean active;

    /** Read-only audit stamps — populated from the entity. */
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
