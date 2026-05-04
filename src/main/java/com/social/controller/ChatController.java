package com.social.controller;


import com.social.Service.ChatService;
import com.social.Service.UserService;
import com.social.models.Chat;
import com.social.models.User;
import com.social.request.CreateChatRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;


    @PostMapping("/api/chats")
    public Chat  creatChat(@RequestHeader("authorization") String jwt,@RequestBody CreateChatRequest req) throws Exception {
        User requser=userService.findUserByJwt(jwt);
        User user2=userService.findUserById(req.getUserId());
        Chat chat=chatService.createChat(requser,user2);
        return chat;

    }

    @GetMapping("/api/chats")
    public List<Chat> findUserChat(@RequestHeader("authorization") String jwt){
        User user=userService.findUserByJwt(jwt);
        List<Chat> chats=chatService.findUserChat(user.getId());
        return chats;

    }
}
