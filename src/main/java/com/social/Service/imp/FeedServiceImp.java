package com.social.Service.imp;

import com.social.Service.FeedService;
import com.social.Service.UserService;
import com.social.exception.UserException;
import com.social.models.Mood;
import com.social.models.Post;
import com.social.models.User;
import com.social.repository.PostRepository;
import com.social.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedServiceImp implements FeedService {

    private final UserService userService;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    public String updateMood(Integer userId, Mood mood) throws UserException {

        User user = userService.findUserById(userId);

        user.setCurrentMood(mood);

        userRepository.save(user);

        return "Mood updated to " + mood;
    }



    @Override
    public List<Post> getFeed(Integer userId) throws UserException {

        User user =userService.findUserById(userId);

        Mood mood = user.getCurrentMood();

        if (mood == Mood.CODING) {
            return postRepository.findByTagsContaining("coding");
        } else if (mood == Mood.STUDY) {
            return postRepository.findByTagsContaining("study");
        } else {
            return postRepository.findByTagsContaining("fun");
        }
    }
}