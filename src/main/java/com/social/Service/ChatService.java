package com.social.Service;

import com.social.models.Chat;
import com.social.models.User;

import java.util.List;

public interface ChatService {

    public Chat createChat(User reqUser,User user2);
    public Chat findChatById(Integer chatId) throws Exception;
    public List<Chat>findUserChat(Integer userId);
}
