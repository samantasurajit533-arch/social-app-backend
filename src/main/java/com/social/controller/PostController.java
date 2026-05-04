package com.social.controller;


import com.social.Responce.ApiResponce;
import com.social.Service.PostService;
import com.social.Service.UserService;
import com.social.models.Post;
import com.social.models.User;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.file.ConfigurationSource;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor

public class PostController {
    private final  PostService postService;
    public  final UserService userService;


    
    @PostMapping("/api/posts")
    public ResponseEntity<Post> createPost(@RequestHeader("Authorization") String jwt,
                                           @RequestBody Post post) throws Exception {


        String token = jwt.startsWith("Bearer ") ? jwt.substring(7) : jwt;


        User reqUser = userService.findUserByJwt(token);

        if (reqUser == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Post created = postService.createNewPost(post, reqUser.getId());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }


    @DeleteMapping("/api/posts/{postId}")
    public  ResponseEntity<ApiResponce>deletedPost(@PathVariable Integer postId, @RequestHeader("Authorization") String jwt) throws Exception {

        User reqUser=userService.findUserByJwt(jwt);
        String message=postService.deletePost(postId,reqUser.getId());
        ApiResponce res=new ApiResponce(message,true);
        return new ResponseEntity<ApiResponce>(res,HttpStatus.OK);

    }

    @GetMapping("/api/posts/{postId}")
    public ResponseEntity<Post>findPostByHandler(@PathVariable Integer postId) throws Exception {
        Post post=postService.findPostById(postId);
        return new ResponseEntity<Post>(HttpStatus.ACCEPTED);

    }
    @GetMapping("/api/posts/user/{userId}")
    public ResponseEntity<List<Post>>findUserPost(@PathVariable Integer userId){
        List<Post>posts=postService.findPostByUserid(userId);
        return new ResponseEntity<List<Post>>(posts,HttpStatus.OK);
    }
    @GetMapping("/api/posts")
    public ResponseEntity<List<Post>>findAllPost(){
        List<Post>posts=postService.findAllPost();
        return new ResponseEntity<List<Post>>(posts,HttpStatus.OK);
    }

    @PutMapping("/api/posts/{postId}")
    public ResponseEntity<Post>savePostHandler(@PathVariable Integer postId, @RequestHeader("Authorization") String jwt
    ) throws Exception {
        User reqUser=userService.findUserByJwt(jwt);
        Post post=postService.savedPost(postId, reqUser.getId());
        return new ResponseEntity<Post>(post,HttpStatus.ACCEPTED);
    }

    @PutMapping("/api/posts/like/{postId}")
    public ResponseEntity<Post>likePostHandler(@PathVariable Integer postId, @RequestHeader("Authorization") String jwt) throws Exception {
        User reqUser=userService.findUserByJwt(jwt);
        Post post=postService.likePost(postId, reqUser.getId());
        return new ResponseEntity<Post>(post,HttpStatus.ACCEPTED);
    }




}
