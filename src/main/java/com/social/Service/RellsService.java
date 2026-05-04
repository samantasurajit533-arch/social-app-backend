package com.social.Service;

import com.social.models.Reels;
import com.social.models.User;

import java.util.List;

public interface RellsService {

    public Reels createTeels(Reels reels, User user);
    public List<Reels> findAllReels();
    public List<Reels> findUsersReel(Integer userId) throws Exception;
}
