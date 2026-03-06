package com.shailesh.recommendation.controller;

import com.shailesh.recommendation.document.UserRecommendation;
import com.shailesh.recommendation.repository.RecommendationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recommendations")
public class RecommendationController {
    private static final Logger log = LoggerFactory.getLogger(RecommendationController.class);

    private final RecommendationRepository recommendationRepository;

    public RecommendationController(RecommendationRepository recommendationRepository) {
        this.recommendationRepository = recommendationRepository;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<RecommendationDto> getRecommendations(@PathVariable Long userId) {
        var rec = recommendationRepository.findByUserId(userId);
        if (rec.isPresent()) {
            return ResponseEntity.ok(toDto(rec.get()));
        }
        return ResponseEntity.notFound().build();
    }

    private RecommendationDto toDto(UserRecommendation rec) {
        return new RecommendationDto(
                rec.getUserId().toString(),
                rec.getRecommendedProductIds(),
                rec.getPreferredCategories(),
                rec.getGeneratedAt(),
                rec.getLastUpdatedAt()
        );
    }

    public record RecommendationDto(
            String userId,
            List<String> recommendedProductIds,
            List<String> preferredCategories,
            java.time.LocalDateTime generatedAt,
            java.time.LocalDateTime lastUpdatedAt
    ) {}
}
