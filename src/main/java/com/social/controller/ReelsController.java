package com.social.controller;


import com.social.Service.RellsService;
import com.social.Service.UserService;
import com.social.models.Reels;
import com.social.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReelsController {
    private  final RellsService rellsService;
    private  final  UserService userService;

    @PostMapping("/api/reels")
    public Reels createReels(@RequestBody Reels reel,
                             @RequestHeader("Authorization") String jwt){
        User requser=userService.findUserByJwt(jwt);

        Reels createedRells=rellsService.createTeels(reel,requser);
        return createedRells;
    }

    @GetMapping("/api/reels")
    public List<Reels> findAllReels(){

        List<Reels>rells=rellsService.findAllReels();
        return  rells;
    }


    @GetMapping("/api/reels/user/{userId}")
    public List<Reels> findUserReels(@PathVariable Integer userId) throws Exception {
        List<Reels>rells=rellsService.findUsersReel(userId);
        return  rells;
    }


}