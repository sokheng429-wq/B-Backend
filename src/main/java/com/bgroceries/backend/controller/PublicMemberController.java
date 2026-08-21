package com.bgroceries.backend.controller;

import com.bgroceries.backend.dto.PublicMemberDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.entity.Member;
import com.bgroceries.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public, unauthenticated read-only view of the team directory for the
 * "Meet Our Team" page. Deliberately returns only display-safe fields
 * (see {@link PublicMemberDto}) — contact/personal data stays behind the
 * authenticated {@code /api/members} endpoints.
 */
@RestController
@RequestMapping("/api/public/members")
@RequiredArgsConstructor
public class PublicMemberController {

    private final MemberRepository memberRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PublicMemberDto>>> getPublicMembers() {
        List<PublicMemberDto> members = memberRepository.findAll().stream()
                .map(this::toPublicDto)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Members retrieved successfully", members));
    }

    private PublicMemberDto toPublicDto(Member member) {
        return new PublicMemberDto(
                member.getId(),
                member.getFullName(),
                member.getPosition(),
                member.getRank(),
                member.getDepartment(),
                member.getCategory(),
                member.getPhotoUrl(),
                member.getMemberDetail() != null ? member.getMemberDetail().getNote() : null
        );
    }
}
