package com.social.controller;


import com.social.Service.FeedService;
import com.social.exception.UserException;
import com.social.models.Mood;
import com.social.models.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class FeedController {

    @Autowired
    private FeedService feedService;

    @PutMapping("/users/{id}/mood")
    public String updateMood(@PathVariable Integer id,
                             @RequestParam Mood mood) throws UserException {
        return feedService.updateMood(id, mood);
    }

    @GetMapping("/feed/{userId}")
    public List<Post> getFeed(@PathVariable Integer userId) throws UserException {
        return feedService.getFeed(userId);
    }
}



