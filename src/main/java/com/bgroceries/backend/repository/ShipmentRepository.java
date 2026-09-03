package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Sale.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByShipCode(String shipCode);

    @Query("SELECT s FROM Shipment s WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           " LOWER(s.shipCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(s.customer) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(s.reference) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(s.deliveryPerson) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:status IS NULL OR :status = '' OR :status = 'ALL' OR s.status = :status) AND " +
           "(:outlet IS NULL OR :outlet = '' OR :outlet = 'ALL' OR s.outlet = :outlet) AND " +
           "(:startDate IS NULL OR s.date >= :startDate) AND " +
           "(:endDate IS NULL OR s.date <= :endDate) " +
           "ORDER BY s.createdAt DESC")
    List<Shipment> searchShipments(
            @Param("search") String search,
            @Param("status") String status,
            @Param("outlet") String outlet,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    List<Shipment> findTop50ByOrderByCreatedAtDesc();
}
