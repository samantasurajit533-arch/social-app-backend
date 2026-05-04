package com.social.Service.imp;

import com.social.Service.RellsService;
import com.social.Service.UserService;
import com.social.models.Reels;
import com.social.models.User;
import com.social.repository.ReelsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class ReelsServiceImplementation implements RellsService {
    private   final  ReelsRepository reelsRepository;
    private  final  UserService userService;

    @Override
    public Reels createTeels(Reels reels, User user) {
        Reels create = new Reels();
        create.setTitle(reels.getTitle());
        create.setUser(user);
        create.setVideo(reels.getVideo());

        // This will no longer be null
        return (Reels) reelsRepository.save(create);
    }

    @Override
    public List<Reels> findAllReels() {
        return reelsRepository.findAll();
    }

    @Override
    public List<Reels> findUsersReel(Integer userId) throws Exception {
        userService.findUserById(userId);
        return reelsRepository.findByUserId(userId);
    }
}
