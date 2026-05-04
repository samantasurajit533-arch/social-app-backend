package com.social.controller;


import com.social.Service.StoryService;
import com.social.Service.UserService;
import com.social.models.Story;
import com.social.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StoryController {
    private  final StoryService storyService;
    private  final UserService userService;

    @PostMapping("/api/story")
    public Story createStory(@RequestBody Story story, @RequestHeader("Authorization") String jwt){
        User requser=userService.findUserByJwt(jwt);
        Story createdStory=storyService.createstory(story,requser);
        return createdStory;

    }
    @GetMapping("/api/story")
    public List<Story> findAllStories() {
        return storyService.findAllStories(); // You'll add this to your service next
    }

    @GetMapping("/api/story/user/{userId}")
    public List<Story>createStory(@PathVariable Integer userid,@RequestHeader("Authorization") String jwt) throws Exception {
        User requser=userService.findUserByJwt(jwt);
        List<Story>Stories=storyService.findStoryByUserId(userid);
        return Stories;

    }

}
