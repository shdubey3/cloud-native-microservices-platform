package com.shailesh.catalog.controller;

import com.shailesh.catalog.document.Product;
import com.shailesh.catalog.repository.ProductRepository;
import com.shailesh.catalog.service.ProductCacheService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/products")
public class CatalogController {
    private static final Logger log = LoggerFactory.getLogger(CatalogController.class);

    private final ProductRepository productRepository;
    private final ProductCacheService cacheService;

    public CatalogController(ProductRepository productRepository, ProductCacheService cacheService) {
        this.productRepository = productRepository;
        this.cacheService = cacheService;
    }

    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return ResponseEntity.ok(products.stream().map(this::toDto).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable String id) {
        // Uses cache-aside pattern
        var product = cacheService.getProduct(id);
        return product.map(p -> ResponseEntity.ok(toDto(p)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductDto>> searchProducts(@RequestParam String name) {
        List<Product> products = productRepository.findByNameContainingIgnoreCase(name);
        return ResponseEntity.ok(products.stream().map(this::toDto).toList());
    }

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody CreateProductRequest request) {
        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .sku(request.sku())
                .quantity(request.quantity())
                .categories(request.categories())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Product saved = productRepository.save(product);
        log.info("Product created: {} ({})", saved.getName(), saved.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody UpdateProductRequest request) {
        var productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Product product = productOpt.get();
        if (request.name() != null) product.setName(request.name());
        if (request.price() != null) product.setPrice(request.price());
        if (request.quantity() != null) product.setQuantity(request.quantity());
        product.setUpdatedAt(LocalDateTime.now());

        Product updated = productRepository.save(product);

        // Invalidate cache
        cacheService.invalidateCache(id);
        log.info("Product updated and cache invalidated: {}", id);

        return ResponseEntity.ok(toDto(updated));
    }

    private ProductDto toDto(Product product) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getSku(),
                product.getQuantity(),
                product.getCategories(),
                product.getRating()
        );
    }

    public record ProductDto(
            String id,
            String name,
            String description,
            java.math.BigDecimal price,
            String sku,
            Integer quantity,
            List<String> categories,
            Double rating
    ) {}

    public record CreateProductRequest(
            @NotBlank String name,
            String description,
            @NotNull @Min(0) java.math.BigDecimal price,
            @NotBlank String sku,
            @NotNull @Min(0) Integer quantity,
            List<String> categories
    ) {}

    public record UpdateProductRequest(
            String name,
            @Min(0) java.math.BigDecimal price,
            @Min(0) Integer quantity
    ) {}
}
