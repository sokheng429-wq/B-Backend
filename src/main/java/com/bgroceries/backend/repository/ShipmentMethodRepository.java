package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Freight.ShipmentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShipmentMethodRepository extends JpaRepository<ShipmentMethod, Long> {
    Optional<ShipmentMethod> findByCode(String code);
    boolean existsByCode(String code);
}
