package com.social.controller;


import com.social.Service.CommentService;
import com.social.Service.UserService;
import com.social.models.Comment;
import com.social.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {

    public final CommentService commentService;
    public final UserService userService;
    @PostMapping("/api/comments/post/{postId}")
    public Comment createComment(@RequestBody Comment comment,
                                 @RequestHeader("Authorization") String jwt,
                                 @PathVariable ("postId") Integer postId) throws Exception {

        User user = userService.findUserByJwt(jwt);

        if (user == null) {
            throw new Exception("User not found for the provided token. Check if the token is valid.");
        }

        return commentService.CreateComment(comment, postId, user.getId());
    }




    @PutMapping("/api/comments/like/{commentId}")
    public Comment likeComment(
                               @RequestHeader("Authorization") String jwt,
                               @PathVariable ("commentId") Integer commentId) throws Exception {

             User user=userService.findUserByJwt(jwt);
            Comment likeComment=commentService.likeComment(commentId,
                user.getId());
        return likeComment;
    }
    @GetMapping("/api/comments")
    public List<Comment>findCommentById(  @RequestHeader("Authorization") String jwt,@PathVariable("commentId")Integer commentId) throws Exception {
        User user=userService.findUserByJwt(jwt);
        Comment find=commentService.findCommentById(commentId);
        return find.getComments();
    }

}
