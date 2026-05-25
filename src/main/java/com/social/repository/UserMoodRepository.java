package com.social.repository;

import com.social.models.UserMood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMoodRepository extends JpaRepository<UserMood, String> {
}

