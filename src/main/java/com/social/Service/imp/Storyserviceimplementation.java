package com.social.Service.imp;

import com.social.Service.StoryService;
import com.social.Service.UserService;
import com.social.models.Story;
import com.social.models.User;
import com.social.repository.StoryRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class Storyserviceimplementation implements StoryService {

    private final StoryRepository storyRepository;

    private  final UserService userService;

    @Override
    public Story createstory(Story story, User user) {

        Story createdStory=new Story();
        createdStory.setCaptions(story.getCaptions());
        createdStory.setImage(story.getImage());
        createdStory.setUser(user);
        createdStory.setTimeStamp(LocalDateTime.now());
        return storyRepository.save(createdStory);
    }

    @Override
    public List<Story> findAllStories() {
        return storyRepository.findAll(); // Fetches all stories from the DB
    }

    @Override
    public List<Story> findStoryByUserId(Integer userId) throws Exception {
        User user=userService.findUserById(userId);
        return storyRepository.findByUserId(userId);
    }




}
