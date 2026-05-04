package com.social.Service.imp;

import com.social.Service.ChatService;
import com.social.Service.MessageService;
import com.social.models.Chat;
import com.social.models.Message;
import com.social.models.User;
import com.social.repository.ChatRepository;
import com.social.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class MessageServiceImplementation  implements MessageService {
    private final MessageRepository messageRepository;
private final ChatService chatService;
private  final ChatRepository chatRepository;

    @Override
    public Message createMessage(User user, Integer chatId,Message req) throws Exception {


        Message  message=new Message();
        Chat chat=chatService.findChatById(chatId);
        message.setChat(chat);
        message.setContent(req.getContent());
        message.setImage(req.getImage());
        message.setUser(user);
        message.setTimeStamp(LocalDateTime.now());
        Message savesMessage=messageRepository.save(message);

        chat.getMessages().add(savesMessage);
        chatRepository.save(chat);
        return savesMessage ;
    }

    @Override
    public List<Message> findChatsMessages(Integer chatId) throws Exception {
       Chat chat=chatService.findChatById(chatId);
        return messageRepository.findByChatId(chatId) ;
    }
}
