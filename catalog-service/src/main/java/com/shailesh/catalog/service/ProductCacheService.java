package com.shailesh.catalog.service;

import com.shailesh.catalog.document.Product;
import com.shailesh.catalog.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Product Caching Service
 *
 * Implements Cache-Aside Pattern:
 * 1. Check if data exists in Redis cache
 * 2. If cache hit, return cached data (fast)
 * 3. If cache miss, fetch from MongoDB
 * 4. Store in Redis with TTL
 * 5. Return data to client
 *
 * Benefits:
 * - Reduced database load
 * - Faster response times
 * - Configurable cache expiration
 */
@Service
public class ProductCacheService {
    private static final Logger log = LoggerFactory.getLogger(ProductCacheService.class);

    private final ProductRepository productRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${cache.product.ttl-minutes:60}")
    private long cacheTtlMinutes;

    private static final String CACHE_KEY_PREFIX = "product:";

    public ProductCacheService(ProductRepository productRepository, RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Get product with caching
     * Implements cache-aside pattern
     */
    public Optional<Product> getProduct(String id) {
        String cacheKey = CACHE_KEY_PREFIX + id;

        try {
            // Try to get from cache
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("Cache HIT for product: {}", id);
                return Optional.of(objectMapper.readValue(cached, Product.class));
            }

            // Cache miss - fetch from MongoDB
            log.debug("Cache MISS for product: {}", id);
            Optional<Product> product = productRepository.findById(id);

            if (product.isPresent()) {
                // Store in cache with TTL
                String productJson = objectMapper.writeValueAsString(product.get());
                redisTemplate.opsForValue()
                        .set(cacheKey, productJson, cacheTtlMinutes, TimeUnit.MINUTES);
                log.debug("Product cached for {} minutes: {}", cacheTtlMinutes, id);
            }

            return product;
        } catch (Exception e) {
            log.error("Error accessing cache: {}", e.getMessage());
            // Fallback to direct database access
            return productRepository.findById(id);
        }
    }

    /**
     * Invalidate product cache on update
     */
    public void invalidateCache(String id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        Boolean deleted = redisTemplate.delete(cacheKey);
        if (deleted != null && deleted) {
            log.debug("Cache invalidated for product: {}", id);
        }
    }
}
