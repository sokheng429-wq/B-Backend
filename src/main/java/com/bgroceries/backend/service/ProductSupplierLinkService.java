package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.ProductSupplierLinkDto;
import com.bgroceries.backend.entity.Stocks.Product;
import com.bgroceries.backend.entity.Stocks.ProductSupplierLink;
import com.bgroceries.backend.entity.Stocks.Supplier;
import com.bgroceries.backend.exception.NotFoundException;
import com.bgroceries.backend.repository.ProductRepository;
import com.bgroceries.backend.repository.ProductSupplierLinkRepository;
import com.bgroceries.backend.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD service for {@code ProductSupplierLink} — manages the purchasing
 * relationship between products and their suppliers.
 */
@Service
@RequiredArgsConstructor
public class ProductSupplierLinkService {

    private final ProductSupplierLinkRepository linkRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;

    @Transactional(readOnly = true)
    public List<ProductSupplierLinkDto> getAll() {
        return linkRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductSupplierLinkDto> getByProductId(Long productId) {
        return linkRepository.findByProductId(productId).stream().map(this::toDto).toList();
    }

    @Transactional
    public ProductSupplierLinkDto create(ProductSupplierLinkDto dto) {
        Product product = findProduct(dto.getProductId());
        Supplier supplier = findSupplier(dto.getSupplierId());

        ProductSupplierLink link = ProductSupplierLink.builder()
                .product(product)
                .supplier(supplier)
                .vendorPartNumber(dto.getVendorPartNumber())
                .contractedCost(dto.getContractedCost())
                .leadTimeDays(dto.getLeadTimeDays())
                .preferred(dto.getPreferred() != null ? dto.getPreferred() : false)
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();

        return toDto(linkRepository.save(link));
    }

    @Transactional
    public ProductSupplierLinkDto update(Long id, ProductSupplierLinkDto dto) {
        ProductSupplierLink link = linkRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Supplier link not found: " + id));

        if (dto.getProductId() != null) {
            link.setProduct(findProduct(dto.getProductId()));
        }
        if (dto.getSupplierId() != null) {
            link.setSupplier(findSupplier(dto.getSupplierId()));
        }
        if (dto.getVendorPartNumber() != null) link.setVendorPartNumber(dto.getVendorPartNumber());
        if (dto.getContractedCost() != null) link.setContractedCost(dto.getContractedCost());
        if (dto.getLeadTimeDays() != null) link.setLeadTimeDays(dto.getLeadTimeDays());
        if (dto.getPreferred() != null) link.setPreferred(dto.getPreferred());
        if (dto.getActive() != null) link.setActive(dto.getActive());

        return toDto(linkRepository.save(link));
    }

    @Transactional
    public void delete(Long id) {
        if (!linkRepository.existsById(id)) {
            throw new NotFoundException("Supplier link not found: " + id);
        }
        linkRepository.deleteById(id);
    }

    // ---- helpers ------------------------------------------------------------

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
    }

    private Supplier findSupplier(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Supplier not found: " + id));
    }

    private ProductSupplierLinkDto toDto(ProductSupplierLink link) {
        return ProductSupplierLinkDto.builder()
                .id(link.getId())
                .productId(link.getProduct().getId())
                .productName(link.getProduct().getName())
                .supplierId(link.getSupplier().getId())
                .supplierName(link.getSupplier().getName())
                .vendorPartNumber(link.getVendorPartNumber())
                .contractedCost(link.getContractedCost())
                .leadTimeDays(link.getLeadTimeDays())
                .preferred(link.getPreferred())
                .active(link.getActive())
                .createdAt(link.getCreatedAt())
                .updatedAt(link.getUpdatedAt())
                .build();
    }
}
