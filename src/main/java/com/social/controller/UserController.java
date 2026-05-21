package com.social.controller;


import com.social.Service.UserService;
import com.social.exception.UserException;
import com.social.models.User;
import com.social.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class UserController {
    public final UserService userService;
    public final UserRepository userRepository;


    @PostMapping("/users")
    public User createUser(@RequestBody User user){
        User savedUser=userService.registerUser(user);
        return savedUser;
    }
    @GetMapping("/api/user")
    public List<User> getUsers(){
        List<User>users=userRepository.findAll();
       return  users;
    }
    // Change "user" to "users" to match your frontend API call
    @GetMapping("/api/users/{userId}")
    public User getUserById(@PathVariable("userId") Integer id) throws UserException {
        User user = userService.findUserById(id);
        return user;
    }

    @PutMapping("/api/users")
    public User updateUser(@RequestBody User user, @RequestHeader("Authorization") String jwt) throws UserException {

        //  Strip "Bearer " from the token string
        String token = jwt;
        if (jwt != null && jwt.startsWith("Bearer ")) {
            token = jwt.substring(7);
        }

        User currentUser = userService.findUserByJwt(token);

        if (currentUser == null) {
            throw new UserException("Current user not found with provided token");
        }


        return userService.updateUser(user, currentUser.getId());
    }


    @PutMapping("/api/users/follow/{userId2}")
    public User followUserHandler(@RequestHeader("Authorization")String jwt,@PathVariable Integer userId2) throws UserException{
        User  reqUser=userService.findUserByJwt(jwt);
        User user=userService.followUser(reqUser.getId(),userId2);
        return user;
    }

    @GetMapping("/api/users/search")
    public List<User> searchUser(@RequestParam(value = "query", name = "query", required = false) String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(); // Return empty list immediately if query is blank
        }
        return userService.searchUser(query);
    }



    @DeleteMapping("/api/users/{userId}")
    public String deleteUser(@PathVariable("userId") Integer userId)throws UserException{
        Optional<User>user=userRepository.findById(userId);
        if(user.isEmpty()){
            throw  new UserException("user not exit with id"+userId);
        }
        userRepository.delete(user.get());

        return "user deleted .successfully with id"+userId;
    }
    @GetMapping("/api/users/profile")
    public ResponseEntity<User> getUserFromToken(@RequestHeader("Authorization") String jwt) throws UserException {
        // Clean "Bearer " prefix if present
        String token = jwt.startsWith("Bearer ") ? jwt.substring(7) : jwt;
        User user = userService.findUserByJwt(token);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(user);
    }

    // New: Handle mood updates via the controller
   // @PutMapping("/{id}/mood")
   // public String updateMood(@PathVariable Integer id, @RequestParam Mood mood) throws UserException {
    //return feedService.updateMood(id, mood);
    }
