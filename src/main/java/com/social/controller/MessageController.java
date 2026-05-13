package com.social.controller;

import com.social.Service.MessageService;
import com.social.Service.UserService;
import com.social.models.Message;
import com.social.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate; // Import this
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate; // 1. Inject the template

    @PostMapping("/api/messages/chat/{chatId}")
    public Message createMessage(
            @RequestBody Message req,
            @PathVariable Integer chatId,
            @RequestHeader("Authorization") String jwt
    ) throws Exception {
        User user = userService.findUserByJwt(jwt);

        Message savedMessage = messageService.createMessage(user, chatId, req);


        messagingTemplate.convertAndSend("/topic/chat/" + chatId.toString(), savedMessage);

        return savedMessage;
    }

    @GetMapping("/api/messages/chat/{chatId}")
    public List<Message> findChatMessage(
            @PathVariable Integer chatId,
            @RequestHeader("Authorization") String jwt
    ) throws Exception {
        User user = userService.findUserByJwt(jwt);
        if (user == null) {
            throw new Exception("User not found or unauthorized");
        }
        return messageService.findChatsMessages(chatId);
    }
}
