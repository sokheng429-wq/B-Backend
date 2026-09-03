package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Sale.ArCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArCollectionRepository extends JpaRepository<ArCollection, Long> {

    Optional<ArCollection> findByCode(String code);

    @Query("SELECT c FROM ArCollection c WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           " LOWER(c.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(c.customer) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(c.contact) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(c.user) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " CAST(c.paidAmount AS string) LIKE CONCAT('%', :search, '%')) AND " +
           "(:status IS NULL OR :status = '' OR :status = 'ALL' OR c.status = :status) AND " +
           "(:startDate IS NULL OR c.paymentDate >= :startDate) AND " +
           "(:endDate IS NULL OR c.paymentDate <= :endDate) " +
           "ORDER BY c.createdAt DESC")
    List<ArCollection> searchCollections(
            @Param("search") String search,
            @Param("status") String status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT c FROM ArCollection c WHERE " +
           "(:field = 'code' AND LOWER(c.code) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           "(:field = 'paidAmount' AND CAST(c.paidAmount AS string) LIKE CONCAT('%', :query, '%')) OR " +
           "(:field = 'rate' AND CAST(c.rate AS string) LIKE CONCAT('%', :query, '%')) OR " +
           "(:field = 'partner' AND LOWER(c.customer) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           "(:field = 'contact' AND LOWER(c.contact) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           "(:field = 'any' AND (LOWER(c.code) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "                     LOWER(c.customer) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "                     LOWER(c.contact) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "                     LOWER(c.user) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "                     CAST(c.paidAmount AS string) LIKE CONCAT('%', :query, '%'))) " +
           "ORDER BY c.createdAt DESC")
    List<ArCollection> searchByField(@Param("field") String field, @Param("query") String query);

    List<ArCollection> findTop50ByOrderByCreatedAtDesc();
}
