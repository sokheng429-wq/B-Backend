package com.bgroceries.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Combined request/response DTO for the Member resource.
 * <p>
 * Used for create, update AND read so all endpoints share one JSON object:
 * <pre>
 * {
 *   "memberCode": "M-001",
 *   "fullName": "Dara Sok",
 *   "position": "Cashier",
 *   "rank": 3,
 *   "department": "Sales",
 *   "category": "Staff",
 *   "detail": {
 *     "phoneNumber": "+85512345678",
 *     "email": "dara@bgroceries.demo",
 *     "address": "Phnom Penh",
 *     "dateOfBirth": "1995-05-10",
 *     "gender": "Male",
 *     "emergencyContact": "+85598765432",
 *     "startDate": "2023-01-15",
 *     "note": "Full-time",
 *     "nationality": "Khmer"
 *   }
 * }
 * </pre>
 * Dates are ISO strings (LocalDate), serialized by Jackson's JavaTimeModule.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto {

    private Long id;

    @NotBlank(message = "Member code is required")
    private String memberCode;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String position;

    private Integer rank;

    private String department;

    private String category;

    private String photoUrl;

    private MemberDetailDto detail;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberDetailDto {

        private String phoneNumber;

        private String email;

        private String address;

        private LocalDate dateOfBirth;

        private String gender;

        private String emergencyContact;

        private LocalDate startDate;

        private String note;

        private String nationality;
    }
}
