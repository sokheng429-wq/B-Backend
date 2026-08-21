package com.bgroceries.backend.controller;

import com.bgroceries.backend.dto.MemberDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Member management endpoints, all wrapped in the standard {@link ApiResponse}
 * envelope. Protected by the existing {@code JwtAuthFilter} (any authenticated
 * user) — no {@code @PreAuthorize} on purpose.
 */
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /** GET /api/members?department=Sales&category=Staff — both filters optional. */
    @GetMapping
    public ResponseEntity<ApiResponse<List<MemberDto>>> getAllMembers(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String category) {
        List<MemberDto> members = memberService.getAllMembers(department, category);
        return ResponseEntity.ok(ApiResponse.success("Members retrieved successfully", members));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberDto>> getMemberById(@PathVariable Long id) {
        MemberDto member = memberService.getMemberById(id);
        return ResponseEntity.ok(ApiResponse.success("Member retrieved successfully", member));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MemberDto>> createMember(@Valid @RequestBody MemberDto dto) {
        MemberDto created = memberService.createMember(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Member created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberDto>> updateMember(@PathVariable Long id, @Valid @RequestBody MemberDto dto) {
        MemberDto updated = memberService.updateMember(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Member updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.ok(ApiResponse.success("Member deleted successfully"));
    }
}
