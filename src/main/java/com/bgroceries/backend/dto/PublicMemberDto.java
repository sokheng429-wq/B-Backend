package com.bgroceries.backend.dto;

/**
 * Slim, public-safe view of a member for the "Meet Our Team" page.
 * Only fields safe to expose without authentication — no phone, email,
 * address, date of birth, or emergency contact.
 */
public record PublicMemberDto(
        Long id,
        String fullName,
        String position,
        Integer rank,
        String department,
        String category,
        String photoUrl,
        String note
) {
}
