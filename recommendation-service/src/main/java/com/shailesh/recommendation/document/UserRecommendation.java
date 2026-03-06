package com.shailesh.recommendation.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * UserRecommendation Document
 *
 * Stores computation recommendations for a user
 */
@Document(collection = "recommendations")
public class UserRecommendation {
    @Id
    private String id;
    private Long userId;
    private List<String> recommendedProductIds;
    private List<String> preferredCategories;
    private LocalDateTime generatedAt;
    private LocalDateTime lastUpdatedAt;

    // No-arg constructor
    public UserRecommendation() {
    }

    // All-args constructor
    public UserRecommendation(String id, Long userId, List<String> recommendedProductIds, List<String> preferredCategories, LocalDateTime generatedAt, LocalDateTime lastUpdatedAt) {
        this.id = id;
        this.userId = userId;
        this.recommendedProductIds = recommendedProductIds;
        this.preferredCategories = preferredCategories;
        this.generatedAt = generatedAt;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    // Getters
    public String getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public List<String> getRecommendedProductIds() {
        return recommendedProductIds;
    }

    public List<String> getPreferredCategories() {
        return preferredCategories;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setRecommendedProductIds(List<String> recommendedProductIds) {
        this.recommendedProductIds = recommendedProductIds;
    }

    public void setPreferredCategories(List<String> preferredCategories) {
        this.preferredCategories = preferredCategories;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    // equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRecommendation that = (UserRecommendation) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(recommendedProductIds, that.recommendedProductIds) &&
                Objects.equals(preferredCategories, that.preferredCategories) &&
                Objects.equals(generatedAt, that.generatedAt) &&
                Objects.equals(lastUpdatedAt, that.lastUpdatedAt);
    }

    // hashCode
    @Override
    public int hashCode() {
        return Objects.hash(id, userId, recommendedProductIds, preferredCategories, generatedAt, lastUpdatedAt);
    }

    // toString
    @Override
    public String toString() {
        return "UserRecommendation{" +
                "id='" + id + '\'' +
                ", userId=" + userId +
                ", recommendedProductIds=" + recommendedProductIds +
                ", preferredCategories=" + preferredCategories +
                ", generatedAt=" + generatedAt +
                ", lastUpdatedAt=" + lastUpdatedAt +
                '}';
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private Long userId;
        private List<String> recommendedProductIds;
        private List<String> preferredCategories;
        private LocalDateTime generatedAt;
        private LocalDateTime lastUpdatedAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder recommendedProductIds(List<String> recommendedProductIds) {
            this.recommendedProductIds = recommendedProductIds;
            return this;
        }

        public Builder preferredCategories(List<String> preferredCategories) {
            this.preferredCategories = preferredCategories;
            return this;
        }

        public Builder generatedAt(LocalDateTime generatedAt) {
            this.generatedAt = generatedAt;
            return this;
        }

        public Builder lastUpdatedAt(LocalDateTime lastUpdatedAt) {
            this.lastUpdatedAt = lastUpdatedAt;
            return this;
        }

        public UserRecommendation build() {
            return new UserRecommendation(id, userId, recommendedProductIds, preferredCategories, generatedAt, lastUpdatedAt);
        }
    }
}
