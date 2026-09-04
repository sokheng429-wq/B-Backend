package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Purchase.ReturnReceiptPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnReceiptPORepository extends JpaRepository<ReturnReceiptPO, Long>, JpaSpecificationExecutor<ReturnReceiptPO> {

    Optional<ReturnReceiptPO> findByReturnPoCode(String returnPoCode);

    @Query("SELECT r FROM ReturnReceiptPO r LEFT JOIN FETCH r.items WHERE r.id = :id")
    Optional<ReturnReceiptPO> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT r.returnPoCode FROM ReturnReceiptPO r WHERE r.returnPoCode LIKE CONCAT(:prefix, '%') ORDER BY r.returnPoCode DESC")
    List<String> findCodesMatchingPrefix(@Param("prefix") String prefix);
}
