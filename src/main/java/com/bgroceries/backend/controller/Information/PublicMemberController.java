package com.bgroceries.backend.controller.Information;

import com.bgroceries.backend.dto.PublicMemberDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.MemberService;
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

    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PublicMemberDto>>> getPublicMembers() {
        List<PublicMemberDto> members = memberService.getPublicMembers();
        return ResponseEntity.ok(ApiResponse.success("Members retrieved successfully", members));
    }
}
