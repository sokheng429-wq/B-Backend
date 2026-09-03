package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Sale.ReturnShipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnShipmentRepository extends JpaRepository<ReturnShipment, Long> {
    Optional<ReturnShipment> findByReturnShipCode(String returnShipCode);

    @Query("SELECT r FROM ReturnShipment r WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           " LOWER(r.returnShipCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(r.soCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(r.customer) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:status IS NULL OR :status = '' OR :status = 'ALL' OR r.status = :status) AND " +
           "(:outlet IS NULL OR :outlet = '' OR :outlet = 'ALL' OR r.outlet = :outlet) AND " +
           "(:startDate IS NULL OR r.date >= :startDate) AND " +
           "(:endDate IS NULL OR r.date <= :endDate) " +
           "ORDER BY r.createdAt DESC")
    List<ReturnShipment> searchReturnShipments(
            @Param("search") String search,
            @Param("status") String status,
            @Param("outlet") String outlet,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    List<ReturnShipment> findTop50ByOrderByCreatedAtDesc();
}
