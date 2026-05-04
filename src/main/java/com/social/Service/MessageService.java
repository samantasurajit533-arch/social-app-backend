package com.social.Service;

import com.social.models.Chat;
import com.social.models.Message;
import com.social.models.User;

import java.util.List;

public interface MessageService {
    public Message createMessage(User user, Integer chatId,Message req) throws Exception;
    public List<Message>findChatsMessages(Integer chatId) throws Exception;
}
