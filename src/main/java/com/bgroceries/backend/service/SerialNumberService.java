package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.SerialNumberDto;
import com.bgroceries.backend.entity.Stocks.Product;
import com.bgroceries.backend.entity.Stocks.SerialNumber;
import com.bgroceries.backend.entity.Stocks.StockLine;
import com.bgroceries.backend.exception.NotFoundException;
import com.bgroceries.backend.repository.ProductRepository;
import com.bgroceries.backend.repository.SerialNumberRepository;
import com.bgroceries.backend.repository.StockLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD service for {@code SerialNumber} — tracks individual serialized or
 * batched product units and their lifecycle status.
 */
@Service
@RequiredArgsConstructor
public class SerialNumberService {

    private final SerialNumberRepository serialNumberRepository;
    private final ProductRepository productRepository;
    private final StockLineRepository stockLineRepository;

    @Transactional(readOnly = true)
    public List<SerialNumberDto> getAll() {
        return serialNumberRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<SerialNumberDto> getByProductId(Long productId) {
        return serialNumberRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public SerialNumberDto create(SerialNumberDto dto) {
        Product product = findProduct(dto.getProductId());

        StockLine stockLine = null;
        if (dto.getStockLineId() != null) {
            stockLine = stockLineRepository.findById(dto.getStockLineId())
                    .orElseThrow(() -> new NotFoundException("StockLine not found: " + dto.getStockLineId()));
        }

        SerialNumber serial = SerialNumber.builder()
                .product(product)
                .stockLine(stockLine)
                .serialNumber(dto.getSerialNumber())
                .batchLot(dto.getBatchLot())
                .expiryDate(dto.getExpiryDate())
                .status(dto.getStatus() != null ? dto.getStatus() : SerialNumber.Status.ACTIVE)
                .productCode(product.getCode())
                .productName(product.getName())
                .build();

        return toDto(serialNumberRepository.save(serial));
    }

    @Transactional
    public SerialNumberDto update(Long id, SerialNumberDto dto) {
        SerialNumber serial = serialNumberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Serial number not found: " + id));

        if (dto.getProductId() != null) {
            Product product = findProduct(dto.getProductId());
            serial.setProduct(product);
            serial.setProductCode(product.getCode());
            serial.setProductName(product.getName());
        }
        if (dto.getStockLineId() != null) {
            serial.setStockLine(stockLineRepository.findById(dto.getStockLineId())
                    .orElseThrow(() -> new NotFoundException("StockLine not found: " + dto.getStockLineId())));
        }
        if (dto.getSerialNumber() != null) serial.setSerialNumber(dto.getSerialNumber());
        if (dto.getBatchLot() != null) serial.setBatchLot(dto.getBatchLot());
        if (dto.getExpiryDate() != null) serial.setExpiryDate(dto.getExpiryDate());
        if (dto.getStatus() != null) serial.setStatus(dto.getStatus());

        return toDto(serialNumberRepository.save(serial));
    }

    @Transactional
    public void delete(Long id) {
        if (!serialNumberRepository.existsById(id)) {
            throw new NotFoundException("Serial number not found: " + id);
        }
        serialNumberRepository.deleteById(id);
    }

    // ---- helpers ------------------------------------------------------------

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
    }

    private SerialNumberDto toDto(SerialNumber serial) {
        return SerialNumberDto.builder()
                .id(serial.getId())
                .productId(serial.getProduct().getId())
                .stockLineId(serial.getStockLine() != null ? serial.getStockLine().getId() : null)
                .serialNumber(serial.getSerialNumber())
                .batchLot(serial.getBatchLot())
                .expiryDate(serial.getExpiryDate())
                .status(serial.getStatus())
                .productCode(serial.getProductCode())
                .productName(serial.getProductName())
                .createdAt(serial.getCreatedAt())
                .build();
    }
}
