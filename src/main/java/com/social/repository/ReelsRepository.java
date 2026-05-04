package com.social.repository;

import com.social.models.Reels;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReelsRepository<E> extends JpaRepository<Reels,Integer> {

    public List<Reels> findByUserId(Integer userId);
}
