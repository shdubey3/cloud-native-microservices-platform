package com.shailesh.recommendation.repository;

import com.shailesh.recommendation.document.UserRecommendation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecommendationRepository extends MongoRepository<UserRecommendation, String> {
    Optional<UserRecommendation> findByUserId(Long userId);
}
