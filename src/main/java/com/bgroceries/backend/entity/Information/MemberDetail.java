package com.bgroceries.backend.entity.Information;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Extended profile of a {@link Member} (one-to-one, owning side of the FK).
 * All columns are nullable so a member can exist without a full detail row.
 */
@Entity
@Table(name = "member_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(length = 100)
    private String email;

    @Column(length = 255)
    private String address;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(length = 20)
    private String gender;

    @Column(name = "emergency_contact", length = 100)
    private String emergencyContact;

    @Column(name = "start_date")
    private LocalDate startDate;

    /** Free-text notes. */
    @Column(length = 1000)
    private String note;

    /** Extra nullable field (bilingual roster display). */
    @Column(length = 100)
    private String nationality;
}
