package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.MemberDto;
import com.bgroceries.backend.entity.Information.Member;
import com.bgroceries.backend.entity.Information.MemberDetail;
import com.bgroceries.backend.exception.ConflictException;
import com.bgroceries.backend.exception.NotFoundException;
import com.bgroceries.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD for the Member resource (one-to-one with {@link MemberDetail}).
 * All writes are transactional; the detail row is cascade-persisted through
 * the owning Member side.
 */
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    /**
     * Lists members, optionally filtered by exact (case-insensitive) department
     * and/or category. Both filters must be non-blank to apply.
     */
    @Transactional(readOnly = true)
    public List<MemberDto> getAllMembers(String department, String category) {
        boolean hasDepartment = department != null && !department.isBlank();
        boolean hasCategory = category != null && !category.isBlank();

        List<Member> members;
        if (hasDepartment && hasCategory) {
            members = memberRepository.findAllByDepartmentIgnoreCaseAndCategoryIgnoreCase(department.trim(), category.trim());
        } else if (hasDepartment) {
            members = memberRepository.findAllByDepartmentIgnoreCase(department.trim());
        } else if (hasCategory) {
            members = memberRepository.findAllByCategoryIgnoreCase(category.trim());
        } else {
            members = memberRepository.findAll();
        }
        return members.stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<com.bgroceries.backend.dto.PublicMemberDto> getPublicMembers() {
        return memberRepository.findAll().stream()
                .map(this::toPublicDto)
                .toList();
    }

    private com.bgroceries.backend.dto.PublicMemberDto toPublicDto(Member member) {
        return new com.bgroceries.backend.dto.PublicMemberDto(
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

    @Transactional(readOnly = true)
    public MemberDto getMemberById(Long id) {
        return toDto(findMember(id));
    }

    @Transactional
    public MemberDto createMember(MemberDto dto) {
        if (memberRepository.existsByMemberCodeIgnoreCase(dto.getMemberCode())) {
            throw new ConflictException("Member code already exists");
        }

        Member member = Member.builder()
                .memberCode(dto.getMemberCode().trim())
                .fullName(dto.getFullName().trim())
                .position(dto.getPosition())
                .rank(dto.getRank())
                .department(dto.getDepartment())
                .category(dto.getCategory())
                .photoUrl(dto.getPhotoUrl())
                .build();
        if (dto.getDetail() != null) {
            member.setMemberDetail(toDetailEntity(dto.getDetail(), member));
        }
        return toDto(memberRepository.save(member));
    }

    @Transactional
    public MemberDto updateMember(Long id, MemberDto dto) {
        Member member = findMember(id);
        if (memberRepository.existsByMemberCodeIgnoreCaseAndIdNot(dto.getMemberCode(), id)) {
            throw new ConflictException("Member code already exists");
        }

        member.setMemberCode(dto.getMemberCode().trim());
        member.setFullName(dto.getFullName().trim());
        member.setPosition(dto.getPosition());
        member.setRank(dto.getRank());
        member.setDepartment(dto.getDepartment());
        member.setCategory(dto.getCategory());
        member.setPhotoUrl(dto.getPhotoUrl());

        // Merge into the existing detail row, or create one if the member has none.
        if (dto.getDetail() != null) {
            MemberDetail detail = member.getMemberDetail();
            if (detail == null) {
                detail = MemberDetail.builder().member(member).build();
                member.setMemberDetail(detail);
            }
            detail.setPhoneNumber(dto.getDetail().getPhoneNumber());
            detail.setEmail(dto.getDetail().getEmail());
            detail.setAddress(dto.getDetail().getAddress());
            detail.setDateOfBirth(dto.getDetail().getDateOfBirth());
            detail.setGender(dto.getDetail().getGender());
            detail.setEmergencyContact(dto.getDetail().getEmergencyContact());
            detail.setStartDate(dto.getDetail().getStartDate());
            detail.setNote(dto.getDetail().getNote());
            detail.setNationality(dto.getDetail().getNationality());
        }
        // A null detail in the request leaves the existing detail untouched.

        return toDto(memberRepository.save(member));
    }

    @Transactional
    public void deleteMember(Long id) {
        // Hard delete; the one-to-one cascade removes the member_detail row.
        memberRepository.delete(findMember(id));
    }

    private Member findMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Member not found"));
    }

    private MemberDto toDto(Member member) {
        MemberDto.MemberDetailDto detailDto = null;
        if (member.getMemberDetail() != null) {
            MemberDetail detail = member.getMemberDetail();
            detailDto = MemberDto.MemberDetailDto.builder()
                    .phoneNumber(detail.getPhoneNumber())
                    .email(detail.getEmail())
                    .address(detail.getAddress())
                    .dateOfBirth(detail.getDateOfBirth())
                    .gender(detail.getGender())
                    .emergencyContact(detail.getEmergencyContact())
                    .startDate(detail.getStartDate())
                    .note(detail.getNote())
                    .nationality(detail.getNationality())
                    .build();
        }
        return MemberDto.builder()
                .id(member.getId())
                .memberCode(member.getMemberCode())
                .fullName(member.getFullName())
                .position(member.getPosition())
                .rank(member.getRank())
                .department(member.getDepartment())
                .category(member.getCategory())
                .photoUrl(member.getPhotoUrl())
                .detail(detailDto)
                .build();
    }

    private MemberDetail toDetailEntity(MemberDto.MemberDetailDto dto, Member member) {
        return MemberDetail.builder()
                .member(member)
                .phoneNumber(dto.getPhoneNumber())
                .email(dto.getEmail())
                .address(dto.getAddress())
                .dateOfBirth(dto.getDateOfBirth())
                .gender(dto.getGender())
                .emergencyContact(dto.getEmergencyContact())
                .startDate(dto.getStartDate())
                .note(dto.getNote())
                .nationality(dto.getNationality())
                .build();
    }
}
