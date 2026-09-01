package com.bgroceries.backend.entity.Sale;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", length = 50, unique = true)
    private String code;

    @Column(name = "customer_name", nullable = false, length = 200)
    private String customerName;

    @Column(name = "second_language", length = 200)
    private String secondLanguage;

    @Column(name = "customer_group", length = 200)
    private String customerGroup;

    @Column(name = "sale_employee", length = 200)
    private String saleEmployee;

    @Column(name = "tax_no", length = 100)
    private String taxNo;

    @Column(name = "payment_term", length = 100)
    private String paymentTerm;

    @Column(name = "terms_and_condition", length = 200)
    private String termsAndCondition;

    @Column(name = "price_book", length = 100)
    private String priceBook;

    @Column(name = "quote_template", length = 200)
    private String quoteTemplate;

    @Column(name = "so_template", length = 200)
    private String soTemplate;

    @Column(name = "invoice_template", length = 200)
    private String invoiceTemplate;

    @Column(name = "do_template", length = 200)
    private String doTemplate;

    @Builder.Default
    @Column(name = "allow_credit", nullable = false)
    private Boolean allowCredit = false;

    @Column(name = "credit_limit", precision = 18, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "current_balance", precision = 18, scale = 2)
    private BigDecimal currentBalance;

    @Column(name = "credit_deposit", precision = 18, scale = 2)
    private BigDecimal creditDeposit;

    @Column(name = "balance", precision = 18, scale = 2)
    private BigDecimal balance;

    @Column(name = "contact_first_name", length = 100)
    private String contactFirstName;

    @Column(name = "contact_last_name", length = 100)
    private String contactLastName;

    @Column(name = "contact_gender", length = 20)
    private String contactGender;

    @Column(name = "contact_dob")
    private LocalDate contactDob;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(name = "contact_mobile", length = 50)
    private String contactMobile;

    @Column(name = "contact_email", length = 200)
    private String contactEmail;

    @Column(name = "contact_website", length = 200)
    private String contactWebsite;

    @Column(name = "address_description", length = 200)
    private String addressDescription;

    @Column(name = "address_second_language", length = 200)
    private String addressSecondLanguage;

    @Column(name = "address_line1", length = 300)
    private String addressLine1;

    @Column(name = "address_line2", length = 300)
    private String addressLine2;

    @Column(name = "address_city", length = 100)
    private String addressCity;

    @Column(name = "address_state", length = 100)
    private String addressState;

    @Column(name = "address_country", length = 100)
    private String addressCountry;

    @Column(name = "address_phone", length = 50)
    private String addressPhone;

    @Column(name = "address_phone_ext", length = 20)
    private String addressPhoneExt;

    @Column(name = "address_fax", length = 50)
    private String addressFax;

    @Column(name = "address_fax_ext", length = 20)
    private String addressFaxExt;

    @Column(name = "address_email", length = 200)
    private String addressEmail;

    @Column(name = "address_website", length = 200)
    private String addressWebsite;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.active == null) this.active = true;
        if (this.allowCredit == null) this.allowCredit = false;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}