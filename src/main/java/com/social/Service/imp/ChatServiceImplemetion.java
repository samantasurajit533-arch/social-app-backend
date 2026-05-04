package com.social.Service.imp;


import com.social.Service.ChatService;
import com.social.models.Chat;
import com.social.models.User;
import com.social.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatServiceImplemetion  implements ChatService {

    private final ChatRepository chatRepository;

    @Override
    public Chat createChat(User reqUser, User user2) {

        Chat isExist=chatRepository.findChatByUserId(user2,reqUser);

        if(isExist!=null){
            return isExist;
        }
        Chat chat=new Chat();
        chat.getUsers().add(user2);
        chat.getUsers().add(reqUser);
        chat.setTimestamp(LocalDateTime.now());
        return chatRepository.save(chat);
    }

    @Override
    public Chat findChatById(Integer chatId) throws Exception {

        Optional<Chat>opt=chatRepository.findById(chatId);
        if(opt.isEmpty()){
            throw new Exception("chat not found with id-"+chatId);
        }
        return opt.get();
    }

    @Override
    public List<Chat> findUserChat(Integer userId) {
        return chatRepository.findByUsersId(userId);
    }
}
