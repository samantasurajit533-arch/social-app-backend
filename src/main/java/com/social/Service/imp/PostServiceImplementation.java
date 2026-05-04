package com.social.Service.imp;


import com.social.Service.PostService;
import com.social.Service.UserService;
import com.social.models.Post;
import com.social.models.User;
import com.social.repository.PostRepository;
import com.social.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostServiceImplementation  implements PostService {

    public final PostRepository postRepository;
    public final UserService userService;
    public final UserRepository userRepository;


    @Override
    public Post createNewPost(Post post, Integer userId) throws Exception {
        User user = userService.findUserById(userId);

        Post newPost = new Post();
        // Ensure all fields from the frontend are mapped correctly
        newPost.setCaption(post.getCaption());
        newPost.setContent(post.getContent()); // <-- Crucial: This fixes the missing data issue
        newPost.setImage(post.getImage());
        newPost.setVideo(post.getVideo());

        // Server-side generated data
        newPost.setCreatedAt(LocalDateTime.now());
        newPost.setUser(user);

        return postRepository.save(newPost);
    }

    @Override
    public String deletePost(Integer postId, Integer userId) throws Exception {
        Post post=findPostById(postId);
        User user=userService.findUserById(userId);
        if(post.getUser().getId()!=user.getId()){
            throw new Exception("you can't delete another user post");
        }
        postRepository.delete(post);
        return  "post deleted successfully";
    }

    @Override
    public List<Post> findPostByUserid(Integer userId) {
        return postRepository.findPostByUserId(userId);
    }

    @Override
    public Post findPostById(Integer postId) throws Exception {

       Optional<Post>opt=postRepository.findById(postId);
       if(opt.isEmpty()){
           throw new Exception("post not found with id"+postId);
       }
        return opt.get();
    }

    @Override
    public List<Post> findAllPost() {
        return postRepository.findAll();
    }

    @Override
    public Post savedPost(Integer postId, Integer userId) throws Exception {
        Post post=findPostById(postId);
        User user=userService.findUserById(userId);

        if(user.getSavedPost().contains(post)){
            user.getSavedPost().remove(post);
        }else{
            user.getSavedPost().add(post);
        }
        userRepository.save(user);
        return post;
    }

    @Override
    public Post likePost(Integer postId, Integer userId) throws Exception {
        Post post = findPostById(postId);
        User user = userService.findUserById(userId);

        if (post.getLiked().contains(user)) {
            post.getLiked().remove(user);
        } else {
            post.getLiked().add(user);
        }

        return postRepository.save(post);
    }

}
