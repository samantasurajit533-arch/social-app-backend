package com.social.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_moods")
@Data
public class UserMood {

    @Id
    private String userId;

    private String detectedMood;

    @Column(columnDefinition = "TEXT")
    private String blockCategories;

    @Column(columnDefinition = "TEXT")
    private String boostCategories;

    private LocalDateTime lastUpdated;
}
