package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.MemberDto;
import com.bgroceries.backend.exception.ConflictException;
import com.bgroceries.backend.exception.NotFoundException;
import com.bgroceries.backend.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CRUD + filter behavior of {@link MemberService} against a real H2 database
 * (dev profile). Hand-rolled — no Mockito (class mocking is broken on JDK 26).
 */
@DataJpaTest
@ActiveProfiles("dev")
class MemberServiceTest {

    @Autowired
    private MemberRepository memberRepository;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberRepository);
    }

    private MemberDto sampleDto(String code) {
        return MemberDto.builder()
                .memberCode(code)
                .fullName("Dara Sok")
                .position("Cashier")
                .rank(3)
                .department("Sales")
                .category("Staff")
                .detail(MemberDto.MemberDetailDto.builder()
                        .phoneNumber("+85512345678")
                        .email("dara@bgroceries.demo")
                        .address("Phnom Penh")
                        .dateOfBirth(LocalDate.of(1995, 5, 10))
                        .gender("Male")
                        .emergencyContact("+85598765432")
                        .startDate(LocalDate.of(2023, 1, 15))
                        .note("Full-time")
                        .nationality("Khmer")
                        .build())
                .build();
    }

    @Test
    void createPersistsMemberWithCascadedDetail() {
        MemberDto created = memberService.createMember(sampleDto("M-001"));

        assertThat(created.getId()).isNotNull();
        assertThat(created.getDetail()).isNotNull();

        MemberDto fetched = memberService.getMemberById(created.getId());
        assertThat(fetched.getMemberCode()).isEqualTo("M-001");
        assertThat(fetched.getFullName()).isEqualTo("Dara Sok");
        assertThat(fetched.getDetail().getEmail()).isEqualTo("dara@bgroceries.demo");
        assertThat(fetched.getDetail().getDateOfBirth()).isEqualTo(LocalDate.of(1995, 5, 10));
        assertThat(fetched.getDetail().getNationality()).isEqualTo("Khmer");
    }

    @Test
    void createRejectsDuplicateMemberCodeIgnoreCase() {
        memberService.createMember(sampleDto("M-001"));

        assertThatThrownBy(() -> memberService.createMember(sampleDto("m-001")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void updateMergesScalarsAndExistingDetail() {
        MemberDto created = memberService.createMember(sampleDto("M-002"));

        created.setFullName("Dara Sok Updated");
        created.setRank(4);
        created.getDetail().setPhoneNumber("+85511111111");
        created.getDetail().setNote("Promoted");

        MemberDto updated = memberService.updateMember(created.getId(), created);

        assertThat(updated.getFullName()).isEqualTo("Dara Sok Updated");
        assertThat(updated.getRank()).isEqualTo(4);
        assertThat(updated.getDetail().getPhoneNumber()).isEqualTo("+85511111111");
        assertThat(updated.getDetail().getNote()).isEqualTo("Promoted");
        assertThat(updated.getDetail().getEmail()).isEqualTo("dara@bgroceries.demo"); // untouched
    }

    @Test
    void updateCreatesDetailWhenMemberHadNone() {
        MemberDto bare = sampleDto("M-006");
        bare.setDetail(null);
        MemberDto created = memberService.createMember(bare);
        assertThat(created.getDetail()).isNull();

        MemberDto withDetail = sampleDto("M-006");
        withDetail.getDetail().setPhoneNumber("+85599999999");

        MemberDto updated = memberService.updateMember(created.getId(), withDetail);

        assertThat(updated.getDetail()).isNotNull();
        assertThat(updated.getDetail().getPhoneNumber()).isEqualTo("+85599999999");
        assertThat(updated.getDetail().getNationality()).isEqualTo("Khmer");
    }

    @Test
    void updateRejectsCodeTakenByAnotherMember() {
        MemberDto first = memberService.createMember(sampleDto("M-003"));
        MemberDto second = memberService.createMember(sampleDto("M-004"));

        second.setMemberCode("m-003");

        assertThatThrownBy(() -> memberService.updateMember(second.getId(), second))
                .isInstanceOf(ConflictException.class);

        // Original member keeps its code.
        assertThat(memberService.getMemberById(first.getId()).getMemberCode()).isEqualTo("M-003");
    }

    @Test
    void getByIdThrowsNotFoundForMissingMember() {
        assertThatThrownBy(() -> memberService.getMemberById(999_999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteRemovesMemberAndCascadesDetail() {
        MemberDto created = memberService.createMember(sampleDto("M-005"));

        memberService.deleteMember(created.getId());

        assertThat(memberRepository.existsById(created.getId())).isFalse();
        assertThatThrownBy(() -> memberService.getMemberById(created.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void filtersByDepartmentAndCategoryIgnoreCase() {
        MemberDto sales = memberService.createMember(sampleDto("M-010"));

        MemberDto ops = sampleDto("M-011");
        ops.setDepartment("Operations");
        ops.setCategory("Contract");
        memberService.createMember(ops);

        List<MemberDto> filtered = memberService.getAllMembers("sales", "staff");
        assertThat(filtered).hasSize(1);
        assertThat(filtered.get(0).getMemberCode()).isEqualTo("M-010");
        assertThat(filtered.get(0).getDetail()).isNotNull();

        assertThat(memberService.getAllMembers("SALES", null)).hasSize(1);
        assertThat(memberService.getAllMembers(null, "Staff")).hasSize(1);
        assertThat(memberService.getAllMembers(null, null)).hasSize(2);
        assertThat(memberService.getAllMembers("", "  ")).hasSize(2); // blank == no filter
        assertThat(sales.getDepartment()).isEqualTo("Sales");
    }
}
