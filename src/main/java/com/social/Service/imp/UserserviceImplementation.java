package com.social.Service.imp;

import com.social.Config.JwtProvider;
import com.social.Service.UserService;
import com.social.exception.UserException;
import com.social.models.User;
import com.social.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserserviceImplementation implements UserService {

    private final UserRepository userRepository;

    @Override
    public User registerUser(User user) {
        // Just save the user object directly.
        // This ensures gender, firstName, lastName, etc., are all saved.
        // Do NOT manually set ID; the database will generate it.
        return userRepository.save(user);
    }

    @Override
    public User findUserById(Integer userId) throws UserException {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            return user.get();
        }
        throw new UserException("User does not exist with userid: " + userId);
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User followUser(Integer reqUserId, Integer userId2) throws UserException {
        User reqUser = findUserById(reqUserId);
        User user2 = findUserById(userId2);

        // Prevent adding duplicate followers
        if(!user2.getFollowers().contains(reqUser.getId())){
            user2.getFollowers().add(reqUser.getId());
            reqUser.getFollowings().add(user2.getId());
        } else {
            // Optional: Logic to Unfollow if already following
            user2.getFollowers().remove(reqUser.getId());
            reqUser.getFollowings().remove(user2.getId());
        }

        userRepository.save(reqUser);
        userRepository.save(user2);
        return reqUser;
    }

    @Override
    public User updateUser(User user, Integer userId) throws UserException {
        User oldUser = findUserById(userId);

        if (user.getFirstName() != null) {
            oldUser.setFirstName(user.getFirstName());
        }
        if (user.getLastName() != null) {
            oldUser.setLastName(user.getLastName());
        }
        if (user.getGender() != null) {
            oldUser.setGender(user.getGender());
        }
        if (user.getProfileImage() != null) {
            oldUser.setProfileImage(user.getProfileImage());
        }
        if (user.getCoverPhoto() != null) {
            oldUser.setCoverPhoto(user.getCoverPhoto());
        }
        // If you want to allow updating email
        if (user.getEmail() != null) {
            oldUser.setEmail(user.getEmail());
        }

        return userRepository.save(oldUser);
    }

    @Override
    public List<User> searchUser(String query) {
        return userRepository.searchUser(query);
    }

    @Override
    public User findUserByJwt(String jwt) {
        // Strip "Bearer " prefix if it exists
        if (jwt != null && jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        }

        String email = JwtProvider.getEmailFromJwtToken(jwt);
        User user = userRepository.findByEmail(email);

        System.out.println("Extracted email from JWT: " + email);
        return user;
    }
}
