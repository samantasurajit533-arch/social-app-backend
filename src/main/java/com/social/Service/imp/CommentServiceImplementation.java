package com.social.Service.imp;

import com.social.Service.CommentService;
import com.social.Service.PostService;
import com.social.Service.UserService;
import com.social.models.Comment;
import com.social.models.Post;
import com.social.models.User;
import com.social.repository.CommentRepository;
import com.social.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentServiceImplementation implements CommentService {

    private final UserService userService;
    private final PostService postService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Override
    public Comment CreateComment(Comment comment, Integer postId, Integer userId) throws Exception {
        // 1. Fetch User and Post
        User user = userService.findUserById(userId);
        Post post = postService.findPostById(postId);

        // 2. Set Comment Details
        comment.setUser(user);
        comment.setCreatedAt(LocalDateTime.now());

        // 3. Save the comment first to get an ID
        Comment savedComment = commentRepository.save(comment);

        // 4. Update the Post's comment list and save the post
        post.getComments().add(savedComment);
        postRepository.save(post);

        return savedComment;
    }

    @Override
    public Comment likeComment(Integer commentId, Integer userId) throws Exception {
        Comment comment = findCommentById(commentId);
        User user = userService.findUserById(userId);

        // Logic: If user already liked it, remove like; otherwise, add like.
        if (comment.getLiked().contains(user)) {
            comment.getLiked().remove(user);
        } else {
            comment.getLiked().add(user);
        }

        return commentRepository.save(comment);
    }

    @Override
    public Comment findCommentById(Integer commentId) throws Exception {
        Optional<Comment> opt = commentRepository.findById(commentId);
        if (opt.isEmpty()) {
            throw new Exception("Comment not found with id: " + commentId);
        }
        return opt.get();
    }
}
