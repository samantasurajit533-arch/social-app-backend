
package com.social.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_moods")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMood {

    @Id
    private String userId;

    private String detectedMood;

    private Integer sadnessScore;

    private Integer angerScore;

    private Integer stressScore;

    private Integer happinessScore;

    private Integer confidenceScore;

    private String riskLevel;

    private Boolean protectionMode;

    @Column(columnDefinition = "TEXT")
    private String blockCategories;

    @Column(columnDefinition = "TEXT")
    private String boostCategories;

    private LocalDateTime lastUpdated;
}
