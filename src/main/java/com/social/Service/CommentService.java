package com.social.Service;

import com.social.models.Comment;
import org.hibernate.annotations.Comments;

public interface CommentService {
    public Comment CreateComment(Comment comment,Integer postId,Integer userId) throws Exception;
    public Comment likeComment(Integer CommentId,Integer userId) throws Exception;
    public Comment findCommentById(Integer CommentId) throws Exception;

}
