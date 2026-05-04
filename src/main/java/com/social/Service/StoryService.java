package com.social.Service;

import com.social.models.Story;
import com.social.models.User;

import java.util.List;

public interface StoryService {

    public Story createstory(Story story, User user);
    public List<Story>findStoryByUserId(Integer userId) throws Exception;
    public List<Story> findAllStories();
}
