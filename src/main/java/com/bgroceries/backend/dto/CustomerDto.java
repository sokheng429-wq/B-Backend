package com.bgroceries.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {

    private Long id;

    @Size(max = 50, message = "Code must be at most 50 characters")
    private String code;

    @NotBlank(message = "Customer name is required")
    @Size(max = 200, message = "Customer name must be at most 200 characters")
    private String customerName;

    @Size(max = 200, message = "Second language must be at most 200 characters")
    private String secondLanguage;

    @Size(max = 200, message = "Customer group must be at most 200 characters")
    private String customerGroup;

    @Size(max = 200, message = "Sale employee must be at most 200 characters")
    private String saleEmployee;

    @Size(max = 100, message = "Tax number must be at most 100 characters")
    private String taxNo;

    @Size(max = 100, message = "Payment term must be at most 100 characters")
    private String paymentTerm;

    @Size(max = 200, message = "Terms and condition must be at most 200 characters")
    private String termsAndCondition;

    @Size(max = 100, message = "Price book must be at most 100 characters")
    private String priceBook;

    @Size(max = 200, message = "Quote template must be at most 200 characters")
    private String quoteTemplate;

    @Size(max = 200, message = "SO template must be at most 200 characters")
    private String soTemplate;

    @Size(max = 200, message = "Invoice template must be at most 200 characters")
    private String invoiceTemplate;

    @Size(max = 200, message = "DO template must be at most 200 characters")
    private String doTemplate;

    private Boolean allowCredit;

    private BigDecimal creditLimit;

    private BigDecimal currentBalance;

    private BigDecimal creditDeposit;

    private BigDecimal balance;

    @Size(max = 100, message = "First name must be at most 100 characters")
    private String contactFirstName;

    @Size(max = 100, message = "Last name must be at most 100 characters")
    private String contactLastName;

    @Size(max = 20, message = "Gender must be at most 20 characters")
    private String contactGender;

    private LocalDate contactDob;

    @Size(max = 50, message = "Phone must be at most 50 characters")
    private String contactPhone;

    @Size(max = 50, message = "Mobile must be at most 50 characters")
    private String contactMobile;

    @Email(message = "Email must be a valid email address")
    @Size(max = 200, message = "Email must be at most 200 characters")
    private String contactEmail;

    @Size(max = 200, message = "Website must be at most 200 characters")
    private String contactWebsite;

    @Size(max = 200, message = "Address description must be at most 200 characters")
    private String addressDescription;

    @Size(max = 200, message = "Second language must be at most 200 characters")
    private String addressSecondLanguage;

    @Size(max = 300, message = "Address must be at most 300 characters")
    private String addressLine1;

    @Size(max = 300, message = "Address 2 must be at most 300 characters")
    private String addressLine2;

    @Size(max = 100, message = "City must be at most 100 characters")
    private String addressCity;

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

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}