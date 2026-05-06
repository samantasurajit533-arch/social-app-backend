package com.social.Service;

import com.social.exception.UserException;
import com.social.models.Mood;
import com.social.models.Post;

import java.util.List;

public interface FeedService {


        String updateMood(Integer userId, Mood mood) throws UserException;
        List<Post> getFeed(Integer userId) throws UserException;
    }
