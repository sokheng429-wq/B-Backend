package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByMemberCodeIgnoreCase(String memberCode);

    boolean existsByMemberCodeIgnoreCaseAndIdNot(String memberCode, Long id);

    Optional<Member> findByMemberCodeIgnoreCase(String memberCode);

    /** All members with their detail eagerly fetched (avoids N+1 on list responses). */
    @Override
    @EntityGraph(attributePaths = "memberDetail")
    List<Member> findAll();

    @Override
    @EntityGraph(attributePaths = "memberDetail")
    Optional<Member> findById(Long id);

    @EntityGraph(attributePaths = "memberDetail")
    List<Member> findAllByDepartmentIgnoreCase(String department);

    @EntityGraph(attributePaths = "memberDetail")
    List<Member> findAllByCategoryIgnoreCase(String category);

    @EntityGraph(attributePaths = "memberDetail")
    List<Member> findAllByDepartmentIgnoreCaseAndCategoryIgnoreCase(String department, String category);
}
