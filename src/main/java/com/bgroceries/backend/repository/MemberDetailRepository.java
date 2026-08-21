package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.MemberDetail;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Minimal — the detail row is always loaded through its owning {@link Member}
 * side, so no custom queries are needed here.
 */
public interface MemberDetailRepository extends JpaRepository<MemberDetail, Long> {
}
