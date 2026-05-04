package com.social.Service;

import com.social.exception.UserException;
import com.social.models.User;

import java.util.List;

public interface UserService {

    public User registerUser(User user);
    public  User findUserById(Integer userId) throws UserException;
    public  User findUserByEmail(String Email);
    public  User followUser(Integer userId1,Integer userId2) throws UserException;
    public User updateUser(User user,Integer Id) throws UserException;
    public List<User>searchUser(String query);
public  User findUserByJwt(String jwt);
}
