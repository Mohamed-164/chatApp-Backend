package com.chatapp.ChatApp.services;

import com.chatapp.ChatApp.model.ChatUnreadedMessageEntity;
import com.chatapp.ChatApp.model.FriendDto;
import com.chatapp.ChatApp.model.UserEntity;
import com.chatapp.ChatApp.repository.ChatUnreadedMessageRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class FriendsService {

    @Autowired
    private ChatUnreadedMessageRespository chatUnreadedMessageRespository;

    public List<FriendDto> getAllFriendsData(Long myId,Set<UserEntity> UserEntities){
        List<FriendDto> friendDtoList = new ArrayList<>();

        for(UserEntity u : UserEntities){

            int unread = 0;

            ChatUnreadedMessageEntity chatUnreadedMessageEntity = chatUnreadedMessageRespository.getUser(u.getNumber(),myId);

            if(chatUnreadedMessageEntity != null){
                unread = chatUnreadedMessageEntity.getCount();
            }

            friendDtoList.add(new FriendDto(u.getNumber(),u.getName(),u.getMail(),u.getProfile_url(),unread,u.getStatus()));
        }

        return friendDtoList;
    }

}
