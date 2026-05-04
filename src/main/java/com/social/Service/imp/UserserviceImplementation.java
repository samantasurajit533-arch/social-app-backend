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

    // Injecting the JwtProvider instance
    private final JwtProvider jwtProvider;

    @Override
    public User registerUser(User user) {
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

        if(!user2.getFollowers().contains(reqUser.getId())){
            user2.getFollowers().add(reqUser.getId());
            reqUser.getFollowings().add(user2.getId());
        } else {
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
        // We use the injected jwtProvider instance instead of the class name
        String email = jwtProvider.getEmailFromJwtToken(jwt);

        User user = userRepository.findByEmail(email);

        System.out.println("Extracted email from JWT: " + email);
        return user;
    }
}
