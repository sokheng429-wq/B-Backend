package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.ProductDto;
import com.bgroceries.backend.entity.Product;
import com.bgroceries.backend.exception.ConflictException;
import com.bgroceries.backend.exception.NotFoundException;
import com.bgroceries.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD for the Product resource (admin Stocks → Products). A non-blank
 * {@code code} must stay unique — duplicates raise ConflictException (409).
 * Blank/absent strings are stored as null so the catalog stays clean.
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<ProductDto> getAllProducts() {
        return productRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductDto getProductById(Long id) {
        return toDto(findProduct(id));
    }

    @Transactional
    public ProductDto createProduct(ProductDto dto) {
        String code = normalize(dto.getCode());
        if (code != null && productRepository.findByCode(code).isPresent()) {
            throw new ConflictException("Product code already exists: " + code);
        }
        Product product = Product.builder()
                .code(code)
                .barCode(normalize(dto.getBarCode()))
                .name(dto.getName().trim())
                .nameKh(normalize(dto.getNameKh()))
                .description(normalize(dto.getDescription()))
                .productGroup(normalize(dto.getProductGroup()))
                .category(normalize(dto.getCategory()))
                .onHand(dto.getOnHand())
                .uom(normalize(dto.getUom()))
                .basePrice(dto.getBasePrice())
                .averageCost(dto.getAverageCost())
                .standardCost(dto.getStandardCost())
                .createDate(dto.getCreateDate())
                .country(normalize(dto.getCountry()))
                .supplier(normalize(dto.getSupplier()))
                .partNumber(normalize(dto.getPartNumber()))
                .brand(normalize(dto.getBrand()))
                .onPo(dto.getOnPo())
                .onSo(dto.getOnSo())
                .availableStock(dto.getAvailableStock())
                .active(dto.getActive() == null || dto.getActive())
                .serial(normalize(dto.getSerial()))
                .expiryDate(dto.getExpiryDate())
                .allowDiscount(dto.getAllowDiscount() == null || dto.getAllowDiscount())
                .tax(dto.getTax())
                .outOfStock(Boolean.TRUE.equals(dto.getOutOfStock()))
                .favorite(Boolean.TRUE.equals(dto.getFavorite()))
                .imageUrl(normalize(dto.getImageUrl()))
                .build();
        return toDto(productRepository.save(product));
    }

    @Transactional
    public ProductDto updateProduct(Long id, ProductDto dto) {
        Product product = findProduct(id);
        String code = normalize(dto.getCode());
        if (code != null && productRepository.existsByCodeAndIdNot(code, id)) {
            throw new ConflictException("Product code already exists: " + code);
        }
        product.setCode(code);
        product.setBarCode(normalize(dto.getBarCode()));
        product.setName(dto.getName().trim());
        product.setNameKh(normalize(dto.getNameKh()));
        product.setDescription(normalize(dto.getDescription()));
        product.setProductGroup(normalize(dto.getProductGroup()));
        product.setCategory(normalize(dto.getCategory()));
        product.setOnHand(dto.getOnHand());
        product.setUom(normalize(dto.getUom()));
        product.setBasePrice(dto.getBasePrice());
        product.setAverageCost(dto.getAverageCost());
        product.setStandardCost(dto.getStandardCost());
        product.setCreateDate(dto.getCreateDate());
        product.setCountry(normalize(dto.getCountry()));
        product.setSupplier(normalize(dto.getSupplier()));
        product.setPartNumber(normalize(dto.getPartNumber()));
        product.setBrand(normalize(dto.getBrand()));
        product.setOnPo(dto.getOnPo());
        product.setOnSo(dto.getOnSo());
        product.setAvailableStock(dto.getAvailableStock());
        if (dto.getActive() != null) product.setActive(dto.getActive());
        product.setSerial(normalize(dto.getSerial()));
        product.setExpiryDate(dto.getExpiryDate());
        if (dto.getAllowDiscount() != null) product.setAllowDiscount(dto.getAllowDiscount());
        product.setTax(dto.getTax());
        product.setOutOfStock(Boolean.TRUE.equals(dto.getOutOfStock()));
        product.setFavorite(Boolean.TRUE.equals(dto.getFavorite()));
        product.setImageUrl(normalize(dto.getImageUrl()));
        return toDto(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = findProduct(id);
        productRepository.delete(product);
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
    }

    /** Trim, and turn blanks into null so optional columns stay clean. */
    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ProductDto toDto(Product p) {
        return ProductDto.builder()
                .id(p.getId())
                .code(p.getCode())
                .barCode(p.getBarCode())
                .name(p.getName())
                .nameKh(p.getNameKh())
                .description(p.getDescription())
                .productGroup(p.getProductGroup())
                .category(p.getCategory())
                .onHand(p.getOnHand())
                .uom(p.getUom())
                .basePrice(p.getBasePrice())
                .averageCost(p.getAverageCost())
                .standardCost(p.getStandardCost())
                .createDate(p.getCreateDate())
                .country(p.getCountry())
                .supplier(p.getSupplier())
                .partNumber(p.getPartNumber())
                .brand(p.getBrand())
                .onPo(p.getOnPo())
                .onSo(p.getOnSo())
                .availableStock(p.getAvailableStock())
                .active(p.getActive())
                .serial(p.getSerial())
                .expiryDate(p.getExpiryDate())
                .allowDiscount(p.getAllowDiscount())
                .tax(p.getTax())
                .outOfStock(p.getOutOfStock())
                .favorite(p.getFavorite())
                .imageUrl(p.getImageUrl())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
